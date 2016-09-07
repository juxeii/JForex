package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.order.process.option.SetOpenPriceOption;

import rx.Completable;

public class SetOpenPriceCommand extends CommonCommand {

    private final IOrder order;
    private final double newOpenPrice;

    private SetOpenPriceCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newOpenPrice = builder.newOpenPrice;
    }

    public final IOrder order() {
        return order;
    }

    public final double newOpenPrice() {
        return newOpenPrice;
    }

    public static final SetOpenPriceOption create(final IOrder order,
                                                  final double newOpenPrice,
                                                  final Function<SetOpenPriceCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newOpenPrice,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<SetOpenPriceOption>
                                 implements SetOpenPriceOption {

        private final IOrder order;
        private final double newOpenPrice;

        private Builder(final IOrder order,
                        final double newOpenPrice,
                        final Function<SetOpenPriceCommand, Completable> startFunction) {
            this.order = order;
            this.newOpenPrice = newOpenPrice;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setOpenPrice(newOpenPrice), order);
            this.callReason = OrderCallReason.CHANGE_PRICE;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(CHANGED_PRICE),
                                                             EnumSet.of(CHANGE_PRICE_REJECTED),
                                                             EnumSet.of(NOTIFICATION));
            this.startFunction = startFunction;
        }

        @Override
        public SetOpenPriceOption doOnSetOpenPriceReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.CHANGE_PRICE_REJECTED, rejectAction);
        }

        @Override
        public SetOpenPriceOption doOnSetOpenPrice(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.CHANGED_PRICE, doneAction);
        }

        @Override
        public SetOpenPriceCommand build() {
            return new SetOpenPriceCommand(this);
        }
    }
}