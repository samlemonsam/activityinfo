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

import com.google.common.base.Optional;

/**
 * An observable that based on an observable optional value.
 *
 * If the optional value is absent, then this observable will be loading.
 *
 * @param <T>
 */
class FlattenedOptional<T> extends Observable<T> {

    private final Observable<Optional<T>> source;
    private Subscription subscription;

    FlattenedOptional(Observable<Optional<T>> source) {
        this.source = source;
    }

    @Override
    protected void onConnect() {
        subscription = source.subscribe(new Observer<Optional<T>>() {
            @Override
            public void onChange(Observable<Optional<T>> observable) {
                FlattenedOptional.this.fireChange();
            }
        });
    }

    @Override
    protected void onDisconnect() {
        subscription.unsubscribe();
        subscription = null;
    }

    @Override
    public boolean isLoading() {
        return source.isLoading() || !source.get().isPresent();
    }

    @Override
    public T get() {
        return source.get().get();
    }
}
