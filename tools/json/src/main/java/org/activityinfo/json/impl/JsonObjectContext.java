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
 * A {@link JsonContext} with String based location index.
 */
class JsonObjectContext extends JsonContext {

    String currentKey;

    public JsonObjectContext(JsonValue value) {
        super(value);
    }

    private JsonValue object() {
        return (JsonValue) getValue();
    }

    public String getCurrentKey() {
        return currentKey;
    }

    @Override
    public void replaceMe(double d) {
        object().put(getCurrentKey(), d);
    }

    @Override
    public void replaceMe(String d) {
        object().put(getCurrentKey(), d);
    }

    @Override
    public void replaceMe(boolean d) {
        object().put(getCurrentKey(), d);
    }

    @Override
    public void replaceMe(JsonValue value) {
        object().put(getCurrentKey(), value);
    }

    public void setCurrentKey(String currentKey) {
        this.currentKey = currentKey;
    }
}
