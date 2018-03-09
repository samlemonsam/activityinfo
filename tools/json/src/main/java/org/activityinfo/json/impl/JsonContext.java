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
package org.activityinfo.json.impl;

import org.activityinfo.json.JsonValue;

/**
 * Represents the current location where a value is stored, and allows
 * the value's replacement or deletion.
 */
abstract class JsonContext {

    private JsonValue value;

    private boolean isFirst = true;

    JsonContext(JsonValue value) {
        this.value = value;
    }

    /**
     * Return the underlying JsonValue (Array or Object) that backs the
     * context.
     */
    public JsonValue getValue() {
        return value;
    }

    /**
     * Whether or not the current context location within the value is the first
     * key or array index.
     */
    public boolean isFirst() {
        return isFirst;
    }

    /**
     * Replace the current location's value with a double.
     */
    public abstract void replaceMe(double d);

    /**
     * Replace the current location's value with a String.
     */
    public abstract void replaceMe(String d);

    /**
     * Replace the current location's value with a boolean.
     */
    public abstract void replaceMe(boolean d);

    /**
     * Replace the current location's value with a JsonValue.
     */
    public abstract void replaceMe(JsonValue value);

    public void setFirst(boolean first) {
        isFirst = first;
    }

    void setValue(JsonValue value) {
        this.value = value;
    }
}
