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

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


/**
 * A list of items whose composition can be externally observed.
 * <p>
 * <p>Once subscribed, a {@link ListObserver} will be fired when items are added, removed from the list,
 * or when the composition of the list changes completely.
 * <p>
 * <p>Note that changes to the items themselves are not broadcast to {@code ListObserver}s.</p>
 *
 * @param <T>
 */
public abstract class ObservableList<T> {
    private final List<ListObserver<T>> observers = new ArrayList<>();

    public abstract boolean isLoading();

    public final Subscription subscribe(final ListObserver<T> observer) {
        if (observers.isEmpty()) {
            onConnect();
        }
        observer.onChange();

        observers.add(observer);

        return new Subscription() {
            @Override
            public void unsubscribe() {
                observers.remove(observer);
                if (observers.isEmpty()) {
                    onDisconnect();
                }
            }
        };
    }


    protected void onConnect() {

    }

    protected void onDisconnect() {

    }


    /**
     * Signal that the list has changed.
     */
    protected final void fireChanged() {
        for (ListObserver<T> observer : observers) {
            observer.onChange();
        }
    }


    /**
     * Signal that the given {@code element} has been added to the list
     */
    protected final void fireAdded(T element) {
        for (ListObserver<T> observer : observers) {
            observer.onElementAdded(element);
        }
    }

    /**
     * Signal that the given {@code element} has been removed from the list
     */
    protected final void fireRemoved(T element) {
        for (ListObserver<T> observer : observers) {
            observer.onElementRemoved(element);
        }
    }

    public final <R> ObservableList<R> map(final Function<T, R> function) {
        return new ObservableListMap<>(this, function);
    }

    public final <R> Observable<List<R>> flatMap(Function<T, Observable<R>> function) {
        ObservableList<Observable<R>> listOfObservables = map(function);
        Observable<List<Observable<R>>> observableListOfObservables = listOfObservables.asObservable();
        return observableListOfObservables.join(new Function<List<Observable<R>>, Observable<List<R>>>() {
            @Nullable
            @Override
            public Observable<List<R>> apply(@Nullable List<Observable<R>> observables) {
                return Observable.flatten(observables);
            }
        });
    }

    /**
     * @return the loaded list of items.
     * @throws AssertionError if the list is still loading.
     */
    public abstract List<T> getList();

    public final Observable<List<T>> asObservable() {
        return new Observable<List<T>>() {

            private Subscription subscription;

            @Override
            public boolean isLoading() {
                return ObservableList.this.isLoading();
            }

            @Override
            public List<T> get() {
                return ObservableList.this.getList();
            }

            @Override
            protected void onConnect() {
                final Observable<List<T>> thisObservable = this;
                subscription = ObservableList.this.subscribe(new ListObserver<T>() {
                    @Override
                    public void onChange() {
                        thisObservable.fireChange();
                    }

                    @Override
                    public void onElementAdded(T element) {
                        thisObservable.fireChange();
                    }

                    @Override
                    public void onElementRemoved(T element) {
                        thisObservable.fireChange();
                    }
                });
            }

            @Override
            protected void onDisconnect() {
                subscription.unsubscribe();
            }
        };
    }
}
