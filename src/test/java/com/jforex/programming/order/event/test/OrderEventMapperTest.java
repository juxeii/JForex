package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.event.OrderEventMapper;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IMessageForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class OrderEventMapperTest extends CommonUtilForTest {

    private OrderEventMapper orderEventMapper;

    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderEventMapper = new OrderEventMapper();
    }

    private OrderMessageData orderMessageData(final IMessage.Type messageType,
                                              final IMessage.Reason... messageReasons) {
        final IMessageForTest message =
                new IMessageForTest(orderUnderTest, messageType, Sets.newHashSet(messageReasons));
        return new OrderMessageData(message);
    }

    private void assertCorrectEventTypeMapping(final OrderEventType expectedType,
                                               final IMessage.Type messageType,
                                               final IMessage.Reason... messageReasons) {
        assertCorrectMapping(expectedType, messageType, messageReasons);
    }

    private void assertCorrectMapping(final OrderEventType expectedType,
                                      final IMessage.Type messageType,
                                      final IMessage.Reason... messageReasons) {
        final OrderMessageData orderMessageData = orderMessageData(messageType, messageReasons);

        final OrderEventType actualType = orderEventMapper.get(orderMessageData);

        assertThat(actualType, equalTo(expectedType));
    }

//    private void assertCorrectEventTypeMappingWithCallRequest(final OrderEventType expectedType,
//                                                              final IMessage.Type messageType,
//                                                              final OrderCallReason orderCallReason,
//                                                              final IMessage.Reason... messageReasons) {
//        assertCorrectMapping(expectedType, messageType, orderCallReason, messageReasons);
//    }

//    private void assertCorrectMapping(final OrderEventType expectedType,
//                                      final IMessage.Type messageType,
//                                      final OrderCallReason orderCallReason,
//                                      final IMessage.Reason... messageReasons) {
//        final OrderMessageData orderMessageData = orderMessageData(messageType, messageReasons);
//
//        final OrderEventType actualType = OrderEventTypeEvaluator.get(orderMessageData);
//
//        assertThat(actualType, equalTo(expectedType));
//    }

    @Test
    public void testNotificationIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.NOTIFICATION,
                                      IMessage.Type.NOTIFICATION);
    }

    @Test
    public void testSubmitRejectedIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.SUBMIT_REJECTED,
                                      IMessage.Type.ORDER_SUBMIT_REJECTED);
    }

    @Test
    public void testSubmitOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.SUBMIT_OK,
                                      IMessage.Type.ORDER_SUBMIT_OK);
    }

    @Test
    public void testFillRejectedIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.FILL_REJECTED,
                                      IMessage.Type.ORDER_FILL_REJECTED);
    }

    @Test
    public void testPartialFillIsMappedCorrect() throws JFException {
        orderUnderTest.setAmount(0.1);
        orderUnderTest.setRequestedAmount(0.2);
        assertCorrectEventTypeMapping(OrderEventType.PARTIAL_FILL_OK,
                                      IMessage.Type.ORDER_FILL_OK);
        assertCorrectEventTypeMapping(OrderEventType.PARTIAL_FILL_OK,
                                      IMessage.Type.ORDER_CHANGED_OK);
    }

    @Test
    public void testChangeRejectIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CHANGED_REJECTED,
                                      IMessage.Type.ORDER_CHANGED_REJECTED);
    }

    @Test
    public void testCloseOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSE_OK,
                                      IMessage.Type.ORDER_CLOSE_OK);
    }

    @Test
    public void testCloseRejectedIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSE_REJECTED,
                                      IMessage.Type.ORDER_CLOSE_REJECTED);
    }

    @Test
    public void testMergeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.MERGE_OK,
                                      IMessage.Type.ORDERS_MERGE_OK);
    }

    @Test
    public void testMergeRejectIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.MERGE_REJECTED,
                                      IMessage.Type.ORDERS_MERGE_REJECTED);
    }

    @Test
    public void testFullFillIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.FULLY_FILLED,
                                      IMessage.Type.ORDER_FILL_OK,
                                      IMessage.Reason.ORDER_FULLY_FILLED);
    }

    @Test
    public void testCloseByMergeIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSED_BY_MERGE,
                                      IMessage.Type.ORDER_CLOSE_OK,
                                      IMessage.Reason.ORDER_CLOSED_BY_MERGE);
    }

    @Test
    public void testCloseBySLIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSED_BY_SL,
                                      IMessage.Type.ORDER_CLOSE_OK,
                                      IMessage.Reason.ORDER_CLOSED_BY_SL);
    }

    @Test
    public void testCloseByTPIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CLOSED_BY_TP,
                                      IMessage.Type.ORDER_CLOSE_OK,
                                      IMessage.Reason.ORDER_CLOSED_BY_TP);
    }

    @Test
    public void testSLChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CHANGED_SL,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_SL);
    }

    @Test
    public void testTPChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CHANGED_TP,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_TP);
    }

    @Test
    public void testLabelChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CHANGED_LABEL,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_LABEL);
    }

    @Test
    public void testAmountChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CHANGED_AMOUNT,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_AMOUNT);
    }

    @Test
    public void testGTTChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CHANGED_GTT,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_GTT);
    }

    @Test
    public void testPriceChangeOKIsMappedCorrect() {
        assertCorrectEventTypeMapping(OrderEventType.CHANGED_PRICE,
                                      IMessage.Type.ORDER_CHANGED_OK,
                                      IMessage.Reason.ORDER_CHANGED_PRICE);
    }

    @Test
    public void testPartialCloseOKIsMappedCorrect() {
        orderUnderTest.setState(IOrder.State.FILLED);
        assertCorrectEventTypeMapping(OrderEventType.PARTIAL_CLOSE_OK,
                                      IMessage.Type.ORDER_CLOSE_OK);
    }

//    @Test
//    public void testLabelChangeRejectIsMappedCorrect() {
//        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_LABEL_REJECTED,
//                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
//                                                     OrderCallReason.CHANGE_LABEL);
//    }

//
//    @Test
//    public void testGTTChangeRejectIsMappedCorrect() {
//        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_GTT_REJECTED,
//                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
//                                                     OrderCallReason.CHANGE_GOOD_TILL_TIME);
//    }

//    @Test
//    public void testAmountChangeRejectIsMappedCorrect() {
//        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_AMOUNT_REJECTED,
//                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
//                                                     OrderCallReason.CHANGE_REQUESTED_AMOUNT);
//    }

//    @Test
//    public void testOpenPriceChangeRejectIsMappedCorrect() {
//        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_OPENPRICE_REJECTED,
//                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
//                                                     OrderCallReason.CHANGE_OPEN_PRICE);
//    }

//    @Test
//    public void testSLChangeRejectIsMappedCorrect() {
//        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_SL_REJECTED,
//                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
//                                                     OrderCallReason.CHANGE_STOP_LOSS_PRICE);
//    }

//    @Test
//    public void testTPChangeRejectIsMappedCorrect() {
//        assertCorrectEventTypeMappingWithCallRequest(OrderEventType.CHANGE_TP_REJECTED,
//                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
//                                                     OrderCallReason.CHANGE_TAKE_PROFIT_PRICE);
//    }
}