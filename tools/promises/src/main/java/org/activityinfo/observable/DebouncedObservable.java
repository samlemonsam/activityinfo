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

import com.google.gwt.user.client.Timer;

class DebouncedObservable<T> extends Observable<T> {

    private final Observable<T> source;
    private final int delay;
    private final Timer timer;
    private Subscription sourceSubscription;

    public DebouncedObservable(Observable<T> source, int delay) {
        this.source = source;
        this.delay = delay;
        this.timer = new Timer() {
            @Override
            public void run() {
                DebouncedObservable.this.fireChange();
            }
        };
    }

    @Override
    protected void onConnect() {
        sourceSubscription = source.subscribe(new Observer<T>() {
            @Override
            public void onChange(Observable<T> observable) {
                if(timer.isRunning()) {
                    timer.cancel();
                }
                timer.schedule(delay);
            }
        });
    }

    @Override
    protected void onDisconnect() {
        sourceSubscription.unsubscribe();
        sourceSubscription = null;
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
