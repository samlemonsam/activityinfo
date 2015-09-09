package org.activityinfo.promise;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

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

    private Map<Function<Void, Promise<Void>>, Integer> retryMap = Maps.newHashMap();
    private List<Function<Void, Promise<Void>>> toRun = Lists.newArrayList();

    private int runningOperationsCount = 0;

    private boolean executeSeriesRunning = false;

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

    public void reset() {
        runningOperationsCount = 0;
        retryMap = Maps.newHashMap();
        toRun = Lists.newArrayList();
    }

    public Promise<Void> executeSerially(final List<Function<Void, Promise<Void>>> operations) {

        if (operations.isEmpty()) {
            return Promise.done();
        }
        if (runningOperationsCount > 0) {
            throw new IllegalStateException("Guard already in running state, it's not allowed to schedule two parallel executions.");
        }

        toRun = Lists.newArrayList(operations);
        retryMap = createRetryMap(operations);

        final Promise<Void> result = new Promise<>();
        executeSeries(result);
        return result;
    }

    private void incrementRetry(Function<Void, Promise<Void>> operation) {
        Integer counter = retryMap.get(operation);
        counter++;
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

            for (final Function<Void, Promise<Void>> operation : Lists.newArrayList(toRun)) {

                debugState();
                if (runningOperationsCount >= maxParallelExecutions) {
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

                        Integer retryCount = retryMap.get(operation);
                        if (retryCount > maxRetryCount) {
                            result.onFailure(caught);
                        } else {
                            executeSeries(result);
                        }
                    }

                    @Override
                    public void onSuccess(Void none) {

                        debugState();
                        runningOperationsCount--;

                        if (toRun.isEmpty()) {
                            result.onSuccess(none);
                        } else {
                            executeSeries(result);
                        }
                    }
                });
            }
        } finally {
            executeSeriesRunning = false;
        }
    }

    private static Map<Function<Void, Promise<Void>>, Integer> createRetryMap(List<Function<Void, Promise<Void>>> operations) {
        Map<Function<Void, Promise<Void>>, Integer> toRun = Maps.newHashMap();
        for (Function<Void, Promise<Void>> operation : operations) {
            toRun.put(operation, 0);
        }
        return toRun;
    }

    private void debugState() {
        GWT.log("runningOperationsCount: " + runningOperationsCount + ", toRun: " + toRun.size());
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
