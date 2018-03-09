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
package org.activityinfo.ui.client.store.tasks;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.promise.Promise;

public abstract class SimpleTask<T> implements Task<T> {

    @Override
    public final TaskExecution start(AsyncCallback<T> callback) {
        return new CancellableExecution<>(execute(), callback);
    }

    protected abstract Promise<T> execute();

    @Override
    public int refreshInterval(T result) {
        return 0;
    }
}
