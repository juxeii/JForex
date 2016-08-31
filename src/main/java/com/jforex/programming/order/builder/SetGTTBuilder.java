package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetGTTBuilder extends OrderBuilder {

    private final IOrder order;
    private final long newGTT;

    public interface SetGTTOption extends CommonOption<SetGTTOption> {
        public SetGTTOption onReject(Consumer<IOrder> rejectAction);

        public SetGTTOption onOK(Consumer<IOrder> okAction);

        public SetGTTBuilder build();
    }

    private SetGTTBuilder(final Builder builder) {
        super(builder);
        order = builder.order;
        newGTT = builder.newGTT;
    }

    public final IOrder order() {
        return order;
    }

    public final long newGTT() {
        return newGTT;
    }

    public static final SetGTTOption forParams(final IOrder order,
                                               final long newGTT) {
        return new Builder(checkNotNull(order), checkNotNull(newGTT));
    }

    private static class Builder extends CommonBuilder<Builder> implements SetGTTOption {

        private final IOrder order;
        private final long newGTT;

        private Builder(final IOrder order,
                        final long newGTT) {
            this.order = order;
            this.newGTT = newGTT;
        }

        @Override
        public SetGTTOption onReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_GTT_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public SetGTTOption onOK(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_GTT, checkNotNull(okAction));
            return this;
        }

        @Override
        public SetGTTBuilder build() {
            return new SetGTTBuilder(this);
        }
    }
}
