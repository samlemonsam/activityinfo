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

import org.activityinfo.json.JsonException;
import org.activityinfo.json.JsonFactory;
import org.activityinfo.json.JsonValue;


/**
 * Implementation of JsonFactory interface using org.json library.
 */
public class JreJsonFactory implements JsonFactory {

    public JsonValue create(String string) {
        assert string != null;
        return new JreJsonString(string);
    }

    @Override
    public JsonValue createFromNullable(String string) {
        if(string == null) {
            return createNull();
        } else {
            return create(string);
        }
    }

    public JsonValue create(double number) {
        return new JreJsonNumber(number);
    }

    public JsonValue create(boolean bool) {
        return new JreJsonBoolean(bool);
    }

    public JsonValue createArray() {
        return new JreJsonArray(this);
    }

    public JsonValue createNull() {
        return JreJsonNull.NULL_INSTANCE;
    }

    public JsonValue createObject() {
        return new JreJsonObject(this);
    }

    public JsonValue parse(String jsonString) throws JsonException {
        if (jsonString.startsWith("(") && jsonString.endsWith(")")) {
            // some clients send in (json) expecting an eval is required
            jsonString = jsonString.substring(1, jsonString.length() - 1);
        }
        return new JsonTokenizer(this, jsonString).nextValue();
    }
}
