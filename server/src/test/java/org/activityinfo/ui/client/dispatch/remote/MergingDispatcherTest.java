package org.activityinfo.ui.client.dispatch.remote;
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

import com.google.gwt.core.client.testing.StubScheduler;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.exception.RetryCountExceedsLimitException;
import org.activityinfo.legacy.shared.util.BackOff;
import org.activityinfo.legacy.shared.util.ExponentialBackOff;
import org.activityinfo.legacy.shared.util.NanoClock;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author yuriyz on 10/17/2014.
 */
public class MergingDispatcherTest {

    private StubScheduler scheduler = new StubScheduler();
    private TimeoutDispatcherMock timeoutDispatcher = new TimeoutDispatcherMock();
    private BackOff backOff = new ExponentialBackOff.Builder()
            .setInitialIntervalMillis(1)
            .setMultiplier(2) // increase in 2 times
            .setNanoClock(new NanoClock() {
                @Override
                public long nanoTime() {
                    return System.nanoTime();
                }
            })
            .build();

    private MergingDispatcher mergingDispatcher = new MergingDispatcher(timeoutDispatcher, scheduler, backOff);

    @Test
    public void retry() {
        final AssertExceptionCallback callback = new AssertExceptionCallback();
        mergingDispatcher.execute(EasyMock.createMock(Command.class), callback);

        // execute initial command as well as retries
        for (int i = 0; i < 2 * RetryCountDown.RETRY_COUNT_LIMIT_ON_TIMEOUT; i++) {
            scheduler.executeCommands();
        }

        assertEquals(timeoutDispatcher.getExecuteCounter(), RetryCountDown.RETRY_COUNT_LIMIT_ON_TIMEOUT);
        callback.assertInstanceof(RetryCountExceedsLimitException.class);
    }
}
