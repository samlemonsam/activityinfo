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
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executors;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author yuriyz on 09/09/2015.
 */
public class PromisesExecutionGuardTest {

    @Test
    public void executeSerially() {
        PromisesExecutionGuard guard = PromisesExecutionGuard.newInstance();

        List<Function<Void, Promise<Void>>> operations = Lists.newArrayList();
        int operationsToRun = 500;
        for (int i = 0; i < operationsToRun; i++) {
            operations.add(createOperation(i, guard));
        }

        final Boolean[] finished = new Boolean[]{false};
        Promise<Void> voidPromise = guard.executeSerially(operations);
        voidPromise.then(new Function<Void, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Void input) {
                System.out.println("Finished all operations!");
                finished[0] = true;
                return null;
            }
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(guard.getLeftOperationToRun(), 0);
        assertTrue("Job is not finished.", finished[0]);
    }

    private Function<Void, Promise<Void>> createOperation(final int i, final PromisesExecutionGuard guard) {
        return new Function<Void, Promise<Void>>() {
            @Nullable
            @Override
            public Promise<Void> apply(Void input) {
                System.out.println("function: " + i + ", runningOperationsCount: " + guard.getRunningOperationsCount() +
                        ", toRun: " + guard.getLeftOperationToRun());

                final Promise promise = new Promise();
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(new java.util.Random().nextInt(10));
                            promise.resolve(null);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            promise.reject(e);
                        }
                    }
                });

                return promise;
            }
        };
    }
}
