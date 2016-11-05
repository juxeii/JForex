package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetOpenPriceParams extends BasicTaskParamsBase {

    private final IOrder order;
    private final double newOpenPrice;

    private SetOpenPriceParams(final Builder builder) {
        super(builder);

        this.order = builder.order;
        this.newOpenPrice = builder.newOpenPrice;
    }

    public IOrder order() {
        return order;
    }

    public double newOpenPrice() {
        return newOpenPrice;
    }

    public static Builder setOpenPriceWith(final IOrder order,
                                           final double newOpenPrice) {
        checkNotNull(order);

        return new Builder(order, newOpenPrice);
    }

    public static class Builder extends ParamsBuilderBase<Builder> {

        private final IOrder order;
        private final double newOpenPrice;

        public Builder(final IOrder order,
                       final double newOpenPrice) {
            this.order = order;
            this.newOpenPrice = newOpenPrice;
        }

        public Builder doOnChangedOpenPrice(final OrderEventConsumer changedOpenPriceConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_PRICE, changedOpenPriceConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_PRICE_REJECTED, changeRejectConsumer);
        }

        public SetOpenPriceParams build() {
            return new SetOpenPriceParams(this);
        }
    }
}
