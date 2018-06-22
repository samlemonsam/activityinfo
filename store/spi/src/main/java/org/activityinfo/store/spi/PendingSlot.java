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
package org.activityinfo.store.spi;

import com.google.common.base.Preconditions;

/**
 * Convenience implementation of Slot that ensures that null pointers
 * are not read or written.
 */
public class PendingSlot<T> implements Slot<T> {

    private T value = null;


    public PendingSlot() {
    }

    public PendingSlot(T value) {
        Preconditions.checkNotNull(value, "Slot value cannot be null");
        this.value = value;
    }

    public void set(T value) {
        Preconditions.checkNotNull(value, "Slot value cannot be null");
        Preconditions.checkState(this.value == null,
                "Slot value has already been set. this.value = " + this.value + ", value = " + value);

        this.value = value;
    }
    
    public boolean isSet() {
        return value != null;
    }
    
    @Override
    public T get() {
        Preconditions.checkState(this.value != null, "Slot value has not been set.");
        return this.value;
    }
}
