/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.promise;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 09/09/2015.
 */
public class PromisesExecutionGuard {

    private static final int DEFAULT_MAX_PARALLEL_EXECUTIONS = 20;
    private static final int DEFAULT_MAX_RETRY_COUNT = 3;

    private final int maxParallelExecutions;
    private final int maxRetryCount;

    private Map<PromiseExecutionOperation, Integer> retryMap = Maps.newHashMap();
    private List<PromiseExecutionOperation> toRun = Lists.newArrayList();

    private int runningOperationsCount = 0;

    // monitor
    @Nullable
    private PromisesExecutionMonitor monitor;
    private PromisesExecutionMonitor.PromisesExecutionStatistic statistic = new PromisesExecutionMonitor.PromisesExecutionStatistic();

    private boolean executeSeriesRunning = false;
    private boolean executeSeriesAgain = false;

    public PromisesExecutionGuard() {
        this(DEFAULT_MAX_PARALLEL_EXECUTIONS, DEFAULT_MAX_RETRY_COUNT);
    }

    public PromisesExecutionGuard(int maxParallelExecutions, int maxRetryCount) {
        this.maxParallelExecutions = maxParallelExecutions;
        this.maxRetryCount = maxRetryCount;
    }

    public static PromisesExecutionGuard newInstance() {
        return new PromisesExecutionGuard();
    }

    public PromisesExecutionGuard withMonitor(@Nullable PromisesExecutionMonitor monitor) {
        this.monitor = monitor;
        return this;
    }

    public void reset() {
        runningOperationsCount = 0;
        retryMap = Maps.newHashMap();
        toRun = Lists.newArrayList();
    }

    public Promise<Void> executeSerially(final List<PromiseExecutionOperation> operations) {

        if (operations.isEmpty()) {
            return Promise.done();
        }
        if (runningOperationsCount > 0) {
            throw new IllegalStateException("Guard already in running state, it's not allowed to schedule two parallel executions.");
        }

        toRun = Lists.newArrayList(operations);
        retryMap = createRetryMap(operations);
        statistic = new PromisesExecutionMonitor.PromisesExecutionStatistic().
                setTotal(operations.size());

        final Promise<Void> result = new Promise<>();
        executeSeries(result);
        return result;
    }

    private void incrementRetry(PromiseExecutionOperation operation) {
        Integer counter = retryMap.get(operation);
        counter++;
        if (counter > 1) {
            statistic.incrementRetry();
        }
        retryMap.put(operation, counter);
    }

    private void executeSeries(final Promise<Void> result) {
        if (executeSeriesRunning) {
            return;
        }
        executeSeriesRunning = true;

        try {

            if (toRun.isEmpty()) {
                return;
            }

            for (final PromiseExecutionOperation operation : Lists.newArrayList(toRun)) {

                debugState();
                if (runningOperationsCount >= maxParallelExecutions) {
                    return;
                }

                Integer retryCount = retryMap.get(operation);
                if (retryCount > maxRetryCount) {
                    statistic.getNotFinishedOperations().clear();
                    statistic.getNotFinishedOperations().addAll(toRun);

                    result.onFailure(new RuntimeException("Exceeds maximum retry count: " + maxRetryCount));
                    return;
                }

                runningOperationsCount++;
                incrementRetry(operation);
                toRun.remove(operation);

                operation.apply(null).then(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {

                        debugState();
                        runningOperationsCount--;
                        toRun.add(operation);

                        triggerMonitor();

                        Integer retryCount = retryMap.get(operation);
                        if (retryCount > maxRetryCount) {
                            statistic.getNotFinishedOperations().clear();
                            statistic.getNotFinishedOperations().addAll(toRun);

                            result.onFailure(caught);
                            return;
                        } else {
                            if (executeSeriesRunning) {
                                executeSeriesAgain = true;
                            } else {
                                executeSeries(result);
                            }
                        }
                    }

                    @Override
                    public void onSuccess(Void none) {

                        runningOperationsCount--;
                        debugState();

                        statistic.incrementCompleted();
                        triggerMonitor();

                        if (toRun.isEmpty()) {
                            if (statistic.getCompleted() == statistic.getTotal()) {
                                result.onSuccess(none);
                            }
                        } else {
                            executeSeries(result);
                        }
                    }
                });
            }
        } finally {
            executeSeriesRunning = false;
            if (executeSeriesAgain) {
                executeSeriesAgain = false;
                executeSeries(result);
            }
        }
    }

    private void triggerMonitor() {
        if (monitor != null) {
            monitor.onChange(statistic);
        }
    }

    private static Map<PromiseExecutionOperation, Integer> createRetryMap(List<PromiseExecutionOperation> operations) {
        Map<PromiseExecutionOperation, Integer> toRun = Maps.newHashMap();
        for (PromiseExecutionOperation operation : operations) {
            toRun.put(operation, 0);
        }
        return toRun;
    }

    private void debugState() {
        GWT.log("runningOperationsCount: " + runningOperationsCount + ", toRun: " + toRun.size() + ", statistic: " + statistic);
    }

    public int getRunningOperationsCount() {
        return runningOperationsCount;
    }

    public int getLeftOperationToRun() {
        return toRun.size();
    }

    public int getMaxParallelExecutions() {
        return maxParallelExecutions;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }
}
