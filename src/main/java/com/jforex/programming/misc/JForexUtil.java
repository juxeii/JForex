package com.jforex.programming.misc;

import static com.google.common.base.Preconditions.checkNotNull;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.order.OrderBasicTask;
import com.jforex.programming.order.OrderCancelSL;
import com.jforex.programming.order.OrderCancelSLAndTP;
import com.jforex.programming.order.OrderCancelTP;
import com.jforex.programming.order.OrderChangeBatch;
import com.jforex.programming.order.OrderCloseTask;
import com.jforex.programming.order.OrderMergeTask;
import com.jforex.programming.order.OrderTaskExecutor;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.command.ClosePositionCommandHandler;
import com.jforex.programming.order.command.MergeCommandHandler;
import com.jforex.programming.order.event.OrderEventFactory;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeDataFactory;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;

public class JForexUtil {

    private final IContext context;
    private IEngine engine;
    private IAccount account;
    private IHistory history;
    private HistoryUtil historyUtil;
    private IDataService dataService;
    private IEngineUtil engineUtil;

    private TickQuoteProvider tickQuoteProvider;
    private TickQuoteRepository tickQuoteRepository;
    private BarQuoteProvider barQuoteProvider;
    private BarQuoteRepository barQuoteRepository;

    private PositionFactory positionFactory;
    private PositionUtil positionUtil;
    private OrderEventGateway orderEventGateway;
    private TaskExecutor taskExecutor;
    private OrderTaskExecutor orderTaskExecutor;
    private OrderUtilHandler orderUtilHandler;
    private final OrderEventTypeDataFactory orderEventTypeDataFactory = new OrderEventTypeDataFactory();
    private OrderBasicTask orderBasicTask;
    private OrderChangeBatch orderChangeBatch;
    private OrderMergeTask orderMergeTask;
    private OrderCloseTask orderCloseTask;
    private MergeCommandHandler mergeCommandHandler;
    private ClosePositionCommandHandler closePositionCommandHandler;
    private OrderCancelSLAndTP orderCancelSLAndTP;
    private OrderCancelSL orderCancelSL;
    private OrderCancelTP orderCancelTP;
    private OrderUtil orderUtil;
    private OrderEventFactory orderEventFactory;

    private final CalculationUtil calculationUtil;

    private final JFHotObservable<TickQuote> tickQuoteObservable = new JFHotObservable<>();
    private final JFHotObservable<BarQuote> barQuoteObservable = new JFHotObservable<>();
    private final JFHotObservable<IMessage> messageObservable = new JFHotObservable<>();
    private final JFHotObservable<OrderCallRequest> callRequestObservable = new JFHotObservable<>();

    public static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    public static final UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public JForexUtil(final IContext context) {
        this.context = checkNotNull(context);

        initContextRelated();
        initInfrastructure();
        initQuoteProvider();
        initOrderRelated();

        calculationUtil = new CalculationUtil(tickQuoteProvider);
    }

    private void initContextRelated() {
        engine = context.getEngine();
        account = context.getAccount();
        history = context.getHistory();
        dataService = context.getDataService();

        historyUtil = new HistoryUtil(history);
    }

    private void initInfrastructure() {
        orderEventFactory = new OrderEventFactory(callRequestObservable.observable());
        orderEventGateway = new OrderEventGateway(messageObservable.observable(), orderEventFactory);
    }

    private void initQuoteProvider() {
        tickQuoteRepository = new TickQuoteRepository(tickQuoteObservable.observable(),
                                                      historyUtil,
                                                      context.getSubscribedInstruments());
        tickQuoteProvider = new TickQuoteProvider(tickQuoteObservable.observable(),
                                                  tickQuoteRepository);
        barQuoteRepository = new BarQuoteRepository(barQuoteObservable.observable(),
                                                    historyUtil);
        barQuoteProvider = new BarQuoteProvider(this,
                                                barQuoteObservable.observable(),
                                                barQuoteRepository);
    }

    private void initOrderRelated() {
        engineUtil = new IEngineUtil(engine);
        taskExecutor = new TaskExecutor(context);
        positionFactory = new PositionFactory(orderEventGateway.observable());
        positionUtil = new PositionUtil(positionFactory);
        orderUtilHandler = new OrderUtilHandler(orderEventGateway,
                                                orderEventTypeDataFactory,
                                                callRequestObservable);
        orderTaskExecutor = new OrderTaskExecutor(taskExecutor, engineUtil);
        orderBasicTask = new OrderBasicTask(orderTaskExecutor, orderUtilHandler);
        orderChangeBatch = new OrderChangeBatch(orderBasicTask);
        orderCancelSL = new OrderCancelSL(orderChangeBatch);
        orderCancelTP = new OrderCancelTP(orderChangeBatch);
        orderCancelSLAndTP = new OrderCancelSLAndTP(orderCancelSL, orderCancelTP);
        mergeCommandHandler = new MergeCommandHandler(orderCancelSLAndTP, orderBasicTask);
        orderMergeTask = new OrderMergeTask(mergeCommandHandler, positionUtil);
        closePositionCommandHandler = new ClosePositionCommandHandler(orderMergeTask,
                                                                      orderChangeBatch,
                                                                      positionUtil);
        orderCloseTask = new OrderCloseTask(closePositionCommandHandler, positionUtil);
        orderUtil = new OrderUtil(orderBasicTask,
                                  orderMergeTask,
                                  orderCloseTask,
                                  positionUtil);
    }

    public IContext context() {
        return context;
    }

    public IEngine engine() {
        return engine;
    }

    public IAccount account() {
        return account;
    }

    public IHistory history() {
        return history;
    }

    public HistoryUtil historyUtil() {
        return historyUtil;
    }

    public TickQuoteProvider tickQuoteProvider() {
        return tickQuoteProvider;
    }

    public BarQuoteProvider barQuoteProvider() {
        return barQuoteProvider;
    }

    public InstrumentUtil instrumentUtil(final Instrument instrument) {
        return new InstrumentUtil(checkNotNull(instrument),
                                  tickQuoteProvider,
                                  barQuoteProvider);
    }

    public CalculationUtil calculationUtil() {
        return calculationUtil;
    }

    public OrderUtil orderUtil() {
        return orderUtil;
    }

    public PositionUtil positionUtil() {
        return positionUtil;
    }

    public void onStop() {
        tickQuoteObservable.unsubscribe();
        barQuoteObservable.unsubscribe();
        messageObservable.unsubscribe();
    }

    public void onMessage(final IMessage message) {
        messageObservable.onNext(checkNotNull(message));
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        checkNotNull(instrument);
        checkNotNull(tick);

        if (shouldForwardQuote(tick.getTime())) {
            final TickQuote tickQuote = new TickQuote(instrument, tick);
            tickQuoteObservable.onNext(tickQuote);
        }
    }

    private boolean shouldForwardQuote(final long time) {
        return !userSettings.enableWeekendQuoteFilter() || !isMarketClosed(time);
    }

    public boolean isMarketClosed() {
        return isMarketClosed(DateTimeUtil.localMillisNow());
    }

    public boolean isMarketClosed(final long time) {
        return dataService.isOfflineTime(time);
    }

    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) {
        checkNotNull(instrument);
        checkNotNull(period);
        checkNotNull(askBar);
        checkNotNull(bidBar);

        onOfferSidedBar(instrument, period, OfferSide.ASK, askBar);
        onOfferSidedBar(instrument, period, OfferSide.BID, bidBar);
    }

    private final void onOfferSidedBar(final Instrument instrument,
                                       final Period period,
                                       final OfferSide offerside,
                                       final IBar askBar) {
        if (shouldForwardQuote(askBar.getTime())) {
            final BarParams quoteParams = BarParams
                .forInstrument(instrument)
                .period(period)
                .offerSide(offerside);
            final BarQuote askBarQuote = new BarQuote(askBar, quoteParams);
            barQuoteObservable.onNext(askBarQuote);
        }
    }

    public void subscribeToBarsFeed(final BarParams barParams) {
        checkNotNull(barParams);

        context.subscribeToBarsFeed(barParams.instrument(),
                                    barParams.period(),
                                    barParams.offerSide(),
                                    this::onOfferSidedBar);
    }

    public static final boolean isStrategyThread() {
        return StringUtils.startsWith(threadName(), platformSettings.strategyThreadPrefix());
    }

    public static final String threadName() {
        return Thread.currentThread().getName();
    }
}
