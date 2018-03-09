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

import com.google.common.base.Objects;

class CachedObservable<T> extends Observable<T> {

    private final Observable<T> source;

    private Subscription sourceSubscription;

    private boolean previouslyLoading;
    private T previousValue;

    public CachedObservable(Observable<T> source) {
        this.source = source;
    }

    @Override
    protected void onConnect() {
        this.sourceSubscription = source.subscribe(new Observer<T>() {
            @Override
            public void onChange(Observable<T> observable) {
                if(observable.isLoading()) {
                    if(!previouslyLoading) {
                        previouslyLoading = true;
                        previousValue = null;
                        CachedObservable.this.fireChange();
                    }
                } else {
                    if(previouslyLoading || !Objects.equal(previousValue, observable.get())) {
                        previouslyLoading = false;
                        previousValue = observable.get();
                        CachedObservable.this.fireChange();
                    }
                }
            }
        });
    }

    @Override
    protected void onDisconnect() {
        sourceSubscription.unsubscribe();
        sourceSubscription = null;
        previousValue = null;
    }

    @Override
    public boolean isLoading() {
        return source.isLoading();
    }

    @Override
    public T get() {
        return source.get();
    }
}
