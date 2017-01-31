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

import org.activityinfo.legacy.shared.exception.RetryCountExceedsLimitException;
import org.activityinfo.legacy.shared.util.BackOff;


/**
 * @author yuriyz on 10/16/2014.
 */
public class RetryCountDown {

    /**
     * In case CommandTimeOutException occurs (may happen if Advisory lock wasn't obtains during configurable time),
     * dispatcher automatically retry command execution. With this constant it's possible to limit number of retry calls.
     */
    public static final int RETRY_COUNT_LIMIT_ON_TIMEOUT = 3;

    private int countDown = RETRY_COUNT_LIMIT_ON_TIMEOUT;
    private BackOff backOff;

    public RetryCountDown(BackOff backOff) {
        this.backOff = backOff;
    }

    public long countDownAndGetWaitPeriod() {
        countDown--;
        if (countDown <= 0) {
            throw new RetryCountExceedsLimitException(); // we don't want to continue retry cycles
        }
        return backOff.nextBackOffMillis();
    }
}
