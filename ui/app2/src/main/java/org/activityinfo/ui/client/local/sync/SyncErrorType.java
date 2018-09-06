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
package org.activityinfo.ui.client.local.sync;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.InvocationException;

public enum SyncErrorType {

    CONNECTION_PROBLEM,

    NEW_VERSION,

    INVALID_AUTH,

    APPCACHE_TIMEOUT,

    UNEXPECTED_EXCEPTION;

    public static SyncErrorType fromException(Throwable caught) {
        if (caught instanceof SyncException) {
            return ((SyncException) caught).getType();
        }
        if (caught instanceof IncompatibleRemoteServiceException) {
            return SyncErrorType.NEW_VERSION;
        } else if (caught instanceof InvocationException) {
            return SyncErrorType.CONNECTION_PROBLEM;
        }
        return SyncErrorType.UNEXPECTED_EXCEPTION;
    }

}
