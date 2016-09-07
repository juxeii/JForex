package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.order.process.option.SetTPOption;

import rx.Completable;

public class SetTPCommand extends CommonCommand {

    private final IOrder order;
    private final double newTP;

    private SetTPCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newTP = builder.newTP;
    }

    public final IOrder order() {
        return order;
    }

    public final double newTP() {
        return newTP;
    }

    public static final SetTPOption create(final IOrder order,
                                           final double newTP,
                                           final Function<SetTPCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newTP,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<SetTPOption>
                                 implements SetTPOption {

        private final IOrder order;
        private final double newTP;

        private Builder(final IOrder order,
                        final double newTP,
                        final Function<SetTPCommand, Completable> startFunction) {
            this.order = order;
            this.newTP = newTP;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setTakeProfitPrice(newTP), order);
            this.callReason = OrderCallReason.CHANGE_TP;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(CHANGED_TP),
                                                             EnumSet.of(CHANGE_TP_REJECTED),
                                                             EnumSet.of(NOTIFICATION));
            this.startFunction = startFunction;
        }

        @Override
        public SetTPOption doOnSetTPReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.CHANGE_TP_REJECTED, rejectAction);
        }

        @Override
        public SetTPOption doOnSetTP(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.CHANGED_TP, doneAction);
        }

        @Override
        public SetTPCommand build() {
            return new SetTPCommand(this);
        }
    }
}