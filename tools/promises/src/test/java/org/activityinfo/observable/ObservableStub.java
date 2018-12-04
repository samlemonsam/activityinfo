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

public class ObservableStub<T> extends Observable<T> {
    
    private boolean loading = true;
    private T value;
    private boolean connected = false;

    public ObservableStub() {
    }

    public ObservableStub(T initialValue) {
        this.loading = false;
        this.value = initialValue;
    }

    public void setToLoading() {
        if(!loading) {
            loading = true;
            fireChange();
        }
    }
    
    public void updateValue(T value) {
        this.loading = false;
        this.value = value;
        fireChange();
    }

    @Override
    protected void onConnect() {
        connected = true;
    }

    @Override
    protected void onDisconnect() {
        connected = false;
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    @Override
    public T get() {
        if(loading) {
            throw new IllegalStateException();
        }
        return value;
    }
}
