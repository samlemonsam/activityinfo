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

/**
 * A {@link TaskExecution} implementation that provides a simple mechanism
 * for cancelling tasks.
 *
 * <p>If the execution is cancelled, the execution's callback will not be invoked.</p>
 */
class CancellableExecution<T> implements TaskExecution {

    private final Promise<T> result;

    private boolean cancelled = false;

    public CancellableExecution(Promise<T> result, AsyncCallback<T> callback) {
        this.result = result;
        this.result.then(new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {
                if(!cancelled) {
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(T result) {
                if(!cancelled) {
                    callback.onSuccess(result);
                }
            }
        });
    }

    @Override
    public boolean isRunning() {
        return !result.isSettled();
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

}
