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
package org.activityinfo.indexedb;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Cursor for traversing or iterating over multiple records in an IndexedDB database.
 *
 * <p>This interface only defines the methods and types required by ActivityInfo and is not
 * a completed description of the IndexedDB API.</p>
 *
 * @param T the type of this cursor's value. Must be either a subclass of
 *          {@link com.google.gwt.core.client.JavaScriptObject} or a type annotated with {@link JsType}
 *
 * See <a href="https://developer.mozilla.org/en-US/docs/Web/API/IDBCursor">IDBCursor API Reference</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface IDBCursor<T> {

    /**
     * Advances the cursor to the next position along its direction, to the
     * item whose key matches the optional key parameter.
     */
    @JsMethod(name = "continue")
    void continue_();

    /**
     * Returns key for the record at the cursor's position as a String. If the cursor is outside its range,
     * this is set to undefined.
     */
    @JsProperty(name = "key")
    String getKeyString();


    @JsProperty(name = "key")
    int getKeyNumber();

    @JsProperty(name = "value")
    T getValue();


    /**
     * in a separate thread, updates the value at the current position of the cursor in the object store.
     * If the cursor points to a record that has just been deleted, a new record is created.
     *
     * @param value the updated value
     */
    void update(T value);
}
