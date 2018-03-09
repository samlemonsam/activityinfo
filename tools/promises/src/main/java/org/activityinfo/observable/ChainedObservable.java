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

public class ChainedObservable<T> extends Observable<T> {

    private Observable<Observable<T>> observable = null;
    private Observable<T> currentValue = null;

    private Subscription observableSubscription = null;
    private Subscription valueSubscription = null;

    public ChainedObservable(Observable<Observable<T>> value) {
        this.observable = value;
    }

    @Override
    public boolean isLoading() {
        return observable.isLoading() || observable.get().isLoading();
    }

    @Override
    public T get() {
        return observable.get().get();
    }

    @Override
    protected void onConnect() {
        assert observableSubscription == null;

        observableSubscription = observable.subscribe(new Observer<Observable<T>>() {
            @Override
            public void onChange(Observable<Observable<T>> observable) {
                unsubscribeFromOldValue();
                if(!observable.isLoading()) {
                    subscribeToNewValue(observable.get());   
                }
            }
        });
    }

    private void unsubscribeFromOldValue() {
        // Unsubscribe from the old value if we had previously been listening
        if(valueSubscription != null) {
            valueSubscription.unsubscribe();
            valueSubscription = null;
        }
    }

    private void subscribeToNewValue(Observable<T> value) {
        valueSubscription = value.subscribe(new Observer<T>() {
            @Override
            public void onChange(Observable<T> observable) {
                ChainedObservable.this.fireChange();
            }
        });
    }

    @Override
    protected void onDisconnect() {
        assert observableSubscription != null;

        observableSubscription.unsubscribe();
        observableSubscription = null;

        if(valueSubscription != null) {
            valueSubscription.unsubscribe();
            valueSubscription = null;
        }
    }
}
