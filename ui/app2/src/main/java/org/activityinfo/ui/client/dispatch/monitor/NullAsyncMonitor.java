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
package org.activityinfo.ui.client.dispatch.monitor;

import org.activityinfo.ui.client.dispatch.AsyncMonitor;

/**
 * An <code>AsyncMonitor</code> that does nothing, and does not allow retries.
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class NullAsyncMonitor implements AsyncMonitor {

    @Override
    public void beforeRequest() {

    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onConnectionProblem() {

    }

    @Override
    public boolean onRetrying() {
        return false;
    }

    @Override
    public void onServerError(Throwable e) {

    }
}
