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

public class Connection<T> {
    private final Observable<T> observable;
    private final Subscription subscription;

    private int changeCount;

    public Connection(Observable<T> observable) {
        this.observable = observable;
        this.subscription = observable.subscribe(this::onChanged);
    }

    private void onChanged(Observable<T> observable) {
        changeCount ++;
    }

    public void resetChangeCounter() {
        changeCount = 0;
    }

    public int getChangeCount() {
        return changeCount;
    }

    public void assertLoading() {
        if (!observable.isLoading()) {
            throw new AssertionError("Expected observable to be loading");
        }
    }

    public T assertLoaded() {
        if (!observable.isLoaded()) {
            throw new AssertionError("Expected observable to have loaded");
        }
        return observable.get();
    }

    public void disconnect() {
        subscription.unsubscribe();
    }


    public void assertChanged() {
        if(changeCount == 0) {
            throw new AssertionError("Expected observable to have changed.");
        }
    }
}
