package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;

import java.util.EnumSet;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class OrderEventTypeDataFactory {

    private final Map<OrderCallReason, OrderEventTypeData> typeDataByCallReason;

    private static final OrderEventTypeData submitData =
            new OrderEventTypeData(EnumSet.of(FULLY_FILLED, SUBMIT_CONDITIONAL_OK),
                                   EnumSet.of(FILL_REJECTED, SUBMIT_REJECTED),
                                   EnumSet.of(NOTIFICATION, SUBMIT_OK, PARTIAL_FILL_OK));
    private static final OrderEventTypeData mergeData =
            new OrderEventTypeData(EnumSet.of(MERGE_OK, MERGE_CLOSE_OK),
                                   EnumSet.of(MERGE_REJECTED),
                                   EnumSet.noneOf(OrderEventType.class));
    private static final OrderEventTypeData closeData =
            new OrderEventTypeData(EnumSet.of(CLOSE_OK),
                                   EnumSet.of(CLOSE_REJECTED),
                                   EnumSet.of(PARTIAL_CLOSE_OK));
    private static final OrderEventTypeData setLabelData =
            new OrderEventTypeData(EnumSet.of(CHANGED_LABEL),
                                   EnumSet.of(CHANGE_LABEL_REJECTED),
                                   EnumSet.noneOf(OrderEventType.class));
    private static final OrderEventTypeData setGTTData =
            new OrderEventTypeData(EnumSet.of(CHANGED_GTT),
                                   EnumSet.of(CHANGE_GTT_REJECTED),
                                   EnumSet.noneOf(OrderEventType.class));
    private static final OrderEventTypeData setAmountData =
            new OrderEventTypeData(EnumSet.of(CHANGED_AMOUNT),
                                   EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                   EnumSet.noneOf(OrderEventType.class));
    private static final OrderEventTypeData setOpenPriceData =
            new OrderEventTypeData(EnumSet.of(CHANGED_PRICE),
                                   EnumSet.of(CHANGE_PRICE_REJECTED),
                                   EnumSet.noneOf(OrderEventType.class));
    private static final OrderEventTypeData setSLData =
            new OrderEventTypeData(EnumSet.of(CHANGED_SL),
                                   EnumSet.of(CHANGE_SL_REJECTED),
                                   EnumSet.noneOf(OrderEventType.class));
    private static final OrderEventTypeData setTPData =
            new OrderEventTypeData(EnumSet.of(CHANGED_TP),
                                   EnumSet.of(CHANGE_TP_REJECTED),
                                   EnumSet.noneOf(OrderEventType.class));

    public OrderEventTypeDataFactory() {
        typeDataByCallReason =
                Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, OrderEventTypeData> builder()
                    .put(OrderCallReason.SUBMIT, submitData)
                    .put(OrderCallReason.MERGE, mergeData)
                    .put(OrderCallReason.CLOSE, closeData)
                    .put(OrderCallReason.CHANGE_LABEL, setLabelData)
                    .put(OrderCallReason.CHANGE_GTT, setGTTData)
                    .put(OrderCallReason.CHANGE_AMOUNT, setAmountData)
                    .put(OrderCallReason.CHANGE_PRICE, setOpenPriceData)
                    .put(OrderCallReason.CHANGE_SL, setSLData)
                    .put(OrderCallReason.CHANGE_TP, setTPData)
                    .build());
    }

    public OrderEventTypeData forCallReason(final OrderCallReason callReason) {
        return typeDataByCallReason.get(callReason);
    }
}
