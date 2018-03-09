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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executors;

import static junit.framework.Assert.assertEquals;

/**
 * @author yuriyz on 09/09/2015.
 */
public class PromisesExecutionGuardTest {

    @Test
    public void executeSerially() {
        PromisesExecutionMonitor monitor = new PromisesExecutionMonitor() {
            @Override
            public void onChange(PromisesExecutionStatistic statistic) {
                //System.out.println("onChange, statistic: " + statistic);
            }
        };
        PromisesExecutionGuard guard = PromisesExecutionGuard.newInstance().withMonitor(monitor);

        //final Boolean[] finished = new Boolean[]{false};
        Promise<Void> voidPromise = guard.executeSerially(createOperations(guard));
        voidPromise.then(new Function<Void, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Void input) {
                System.out.println("Finished all operations!");
                //finished[0] = true;
                return null;
            }
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(guard.getLeftOperationToRun(), 0);
        //assertTrue("Job is not finished.", finished[0]);
    }

    @Test
    public void executeSeriallyWithRetry() {
        PromisesExecutionMonitor monitor = new PromisesExecutionMonitor() {
            @Override
            public void onChange(PromisesExecutionStatistic statistic) {
                //System.out.println("onChange, statistic: " + statistic);
            }
        };
        PromisesExecutionGuard guard = PromisesExecutionGuard.newInstance().withMonitor(monitor);

        List<PromiseExecutionOperation> operations = createOperations(guard);
        operations.add(new PromiseExecutionOperation() {
            @Nullable
            @Override
            public Promise<Void> apply(@Nullable Void input) {
                return Promise.rejected(new RuntimeException("fail"));
            }
        });

        //final Boolean[] finished = new Boolean[]{false};
        Promise<Void> voidPromise = guard.executeSerially(operations);
        voidPromise.then(new Function<Void, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Void input) {
                System.out.println("Finished all operations!");
                //finished[0] = true;
                return null;
            }
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(guard.getLeftOperationToRun(), 1);
        //assertFalse("Job is finished.", finished[0]);
    }

    private List<PromiseExecutionOperation> createOperations(PromisesExecutionGuard guard) {
        List<PromiseExecutionOperation> operations = Lists.newArrayList();
        int operationsToRun = 500;
        for (int i = 1; i <= operationsToRun; i++) {
            operations.add(createOperation(i, guard));
        }
        return operations;
    }

    private PromiseExecutionOperation createOperation(final int i, final PromisesExecutionGuard guard) {
        return new PromiseExecutionOperation() {
            @Nullable
            @Override
            public Promise<Void> apply(Void input) {
//                System.out.println("function: " + i + ", runningOperationsCount: " + guard.getRunningOperationsCount() +
//                        ", toRun: " + guard.getLeftOperationToRun());

                final Promise promise = new Promise();
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                              @Override
                              public void run() {
                                  try {
                                      Thread.sleep(new java.util.Random().nextInt(3));
                                  } catch (InterruptedException e) {
                                      e.printStackTrace();
                                  }
                                  promise.resolve(null);
                              }
                          }

                );

                return promise;
            }
        };
    }
}
