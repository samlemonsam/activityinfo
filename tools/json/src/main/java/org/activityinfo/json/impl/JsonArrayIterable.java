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

import java.util.Iterator;

public class JsonArrayIterable implements Iterable<JsonValue> {

    private final JsonValue array;

    public JsonArrayIterable(JsonValue array) {
        this.array = array;
    }

    @Override
    public Iterator<JsonValue> iterator() {

        final int length = array.length();

        return new Iterator<JsonValue>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public JsonValue next() {
                return array.get(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
