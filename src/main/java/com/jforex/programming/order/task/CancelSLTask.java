package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.BatchCancelSLParams;

import io.reactivex.Observable;

public class CancelSLTask {

    private final BatchChangeTask batchChangeTask;
    private final TaskParamsUtil taskParamsUtil;

    public CancelSLTask(final BatchChangeTask batchChangeTask,
                        final TaskParamsUtil taskParamsUtil) {
        this.batchChangeTask = batchChangeTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelSLSLOrders,
                                          final BatchCancelSLParams batchCancelSLParams) {
        final Instrument instrument = toCancelSLSLOrders.iterator().next().getInstrument();

        return taskParamsUtil.composePositionTask(instrument,
                                                  batchCancelSL(toCancelSLSLOrders, batchCancelSLParams),
                                                  batchCancelSLParams);
    }

    private Observable<OrderEvent> batchCancelSL(final Collection<IOrder> toCancelSLSLOrders,
                                                 final BatchCancelSLParams batchCancelSLParams) {
        return Observable.defer(() -> batchChangeTask.cancelSL(toCancelSLSLOrders,
                                                               batchCancelSLParams.cancelSLParams(),
                                                               batchCancelSLParams.batchMode()));
    }
}
