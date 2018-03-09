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
package org.activityinfo.model.form;

import org.activityinfo.json.JsonValue;

/**
 * Support methods for JSON string
 */
public class JsonParsing {
    
    public static String toNullableString(JsonValue value) {
        if(value.isJsonNull()) {
            return null;
        } else {
            return value.asString();
        }
    }
    
    public static String fromEnumValue(JsonValue element) {
        if(element.isJsonObject()) {
            JsonValue object = element;
            if(object.hasKey("value")) {
                return object.get("value").asString().toUpperCase();
            }
            if(object.hasKey("id")) {
                return object.get("id").asString().toUpperCase();
            }
        } else if(element.isJsonPrimitive()) {
            return element.asString().toUpperCase();
        }
        throw new IllegalArgumentException("element: " + element);
    }
    
}
