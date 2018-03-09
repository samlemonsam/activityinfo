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
package org.activityinfo.observable;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.promise.Promise;

/**
 * Created by yuriyz on 4/25/2016.
 */
public class ObservablePromise<T> extends Observable<T> {

    private final Promise<T> promise;

    public ObservablePromise(Promise<T> promise) {
        this.promise = promise;
        promise.then(new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {
                fireChange();
            }

            @Override
            public void onSuccess(T result) {
                fireChange();
            }
        });
    }

    @Override
    public boolean isLoading() {
        return !promise.isSettled();
    }

    @Override
    public T get() {
        if (promise.getState() == Promise.State.FULFILLED) {
            return promise.get();
        } else {
            return null;
        }
    }

    public Promise<T> getPromise() {
        return promise;
    }
}
