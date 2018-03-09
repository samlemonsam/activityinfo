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

import com.google.common.base.Preconditions;

import java.util.Objects;

public class StatefulValue<T> extends Observable<T> {
    
    private T value;

    public StatefulValue() {
        value = null;
    }

    public StatefulValue(T value) {
        this.value = value;
    }

    @Override
    public boolean isLoading() {
        return value == null;
    }

    /**
     * Updates the reference to this state's value and notifies subscribers.
     */
    public void updateValue(T value) {
        Preconditions.checkNotNull(value, "value cannot be null");
        this.value = value;
        fireChange();
    }

    public boolean updateIfNotEqual(T value) {
        if (!Objects.equals(this.value, value)) {
            this.value = value;
            fireChange();
            return true;
        } else {
            return false;
        }
    }

    public boolean updateIfNotSame(T value) {
        if(this.value != value) {
            this.value = value;
            fireChange();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Notify subscribers that this state's value has changed.
     */
    public void updated() {
        fireChange();
    }
    
    public void clear() {
        if(value != null) {
            value = null;
            fireChange();
        };
    }

    @Override
    public T get() {
        if(value == null) {
            throw new IllegalStateException();
        }
        return value;
    }


}
