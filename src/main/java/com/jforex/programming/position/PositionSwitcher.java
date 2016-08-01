package com.jforex.programming.position;

import java.util.Map;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.common.collect.ImmutableMap;
import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.settings.UserSettings;

import rx.Observable;

public final class PositionSwitcher {

    private final OrderUtil orderUtil;
    private final PositionOrders positionOrders;
    private final Instrument instrument;
    private final OrderParamsSupplier orderParamsSupplier;
    private final StateMachineConfig<FSMState, FSMTrigger> fsmConfig = new StateMachineConfig<>();
    private final StateMachine<FSMState, FSMTrigger> fsm = new StateMachine<>(FSMState.FLAT, fsmConfig);
    private Map<OrderDirection, FSMState> nextStatesByDirection;

    public enum FSMState {

        FLAT,
        LONG,
        SHORT,
        BUSY
    }

    public enum FSMTrigger {

        FLAT,
        BUY,
        SELL,
        MERGE_DONE,
        CLOSE_DONE
    }

    private static final UserSettings userSettings = ConfigFactory.create(UserSettings.class);
    private static final String defaultMergePrefix = userSettings.defaultMergePrefix();

    public PositionSwitcher(final Instrument instrument,
                            final OrderParamsSupplier orderParamsSupplier,
                            final OrderUtil orderUtil) {
        this.instrument = instrument;
        this.orderParamsSupplier = orderParamsSupplier;
        this.orderUtil = orderUtil;

        positionOrders = orderUtil.positionOrders(instrument);
        configureFSM();
    }

    private final void configureFSM() {
        nextStatesByDirection = ImmutableMap
                .<OrderDirection, FSMState> builder()
                .put(OrderDirection.FLAT, FSMState.FLAT)
                .put(OrderDirection.LONG, FSMState.LONG)
                .put(OrderDirection.SHORT, FSMState.SHORT)
                .build();

        fsmConfig
                .configure(FSMState.FLAT)
                .permit(FSMTrigger.BUY, FSMState.BUSY)
                .permit(FSMTrigger.SELL, FSMState.BUSY)
                .ignore(FSMTrigger.FLAT)
                .ignore(FSMTrigger.CLOSE_DONE)
                .ignore(FSMTrigger.MERGE_DONE);

        fsmConfig
                .configure(FSMState.LONG)
                .permit(FSMTrigger.FLAT, FSMState.BUSY)
                .permit(FSMTrigger.SELL, FSMState.BUSY)
                .ignore(FSMTrigger.BUY)
                .ignore(FSMTrigger.CLOSE_DONE)
                .ignore(FSMTrigger.MERGE_DONE);

        fsmConfig
                .configure(FSMState.SHORT)
                .permit(FSMTrigger.FLAT, FSMState.BUSY)
                .permit(FSMTrigger.BUY, FSMState.BUSY)
                .ignore(FSMTrigger.SELL)
                .ignore(FSMTrigger.CLOSE_DONE)
                .ignore(FSMTrigger.MERGE_DONE);

        fsmConfig
                .configure(FSMState.BUSY)
                .onEntryFrom(FSMTrigger.BUY, () -> executeOrderCommandSignal(OrderDirection.LONG))
                .onEntryFrom(FSMTrigger.SELL, () -> executeOrderCommandSignal(OrderDirection.SHORT))
                .onEntryFrom(FSMTrigger.FLAT, this::closePosition)
                .permitDynamic(FSMTrigger.MERGE_DONE,
                               () -> nextStatesByDirection.get(positionOrders.direction()))
                .permit(FSMTrigger.CLOSE_DONE, FSMState.FLAT)
                .ignore(FSMTrigger.FLAT)
                .ignore(FSMTrigger.BUY)
                .ignore(FSMTrigger.SELL);
    }

    public final void sendBuySignal() {
        fsm.fire(FSMTrigger.BUY);
    }

    public final void sendSellSignal() {
        fsm.fire(FSMTrigger.SELL);
    }

    public final void sendFlatSignal() {
        fsm.fire(FSMTrigger.FLAT);
    }

    private void closePosition() {
        orderUtil
                .closePosition(instrument)
                .retryWhen(StreamUtil::retryObservable)
                .doOnTerminate(() -> fsm.fire(FSMTrigger.CLOSE_DONE))
                .subscribe();
    }

    private final void executeOrderCommandSignal(final OrderDirection desiredDirection) {
        final OrderCommand newOrderCommand = OrderStaticUtil.directionToCommand(desiredDirection);
        final OrderParams adaptedOrderParams = adaptedOrderParams(newOrderCommand);
        final String mergeLabel = defaultMergePrefix + adaptedOrderParams.label();

        orderUtil
                .submitOrder(adaptedOrderParams)
                .concatWith(Observable.defer(() -> orderUtil.mergePositionOrders(mergeLabel, instrument)))
                .retryWhen(StreamUtil::retryObservable)
                .doOnTerminate(() -> fsm.fire(FSMTrigger.MERGE_DONE))
                .subscribe();
    }

    private final OrderParams adaptedOrderParams(final OrderCommand newOrderCommand) {
        final double absPositionExposure = Math.abs(positionOrders.signedExposure());
        final OrderParams orderParams = orderParamsSupplier.forCommand(newOrderCommand);

        return orderParams
                .clone()
                .withOrderCommand(newOrderCommand)
                .withAmount(orderParams.amount() + absPositionExposure)
                .build();
    }
}
