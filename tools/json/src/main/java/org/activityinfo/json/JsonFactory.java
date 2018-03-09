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
package org.activityinfo.json;

/**
 * Factory interface for parsing and creating JSON objects.
 */
public interface JsonFactory {

    /**
     * Create a JsonString from a Java String.
     *
     * @param string a Java String
     * @return the parsed JsonString
     */
    JsonValue create(String string);

    /**
     * Create a JsonValue from a Java String that may be null
     *
     * @param string a Java String
     * @return either JsonNull or JsonString
     */
    JsonValue createFromNullable(String string);

    /**
     * Create a JsonNumber from a Java double.
     *
     * @param number a Java double
     * @return the parsed JsonNumber
     */
    JsonValue create(double number);

    /**
     * Create a JsonBoolean from a Java boolean.
     *
     * @param bool a Java boolean
     * @return the parsed JsonBoolean
     */
    JsonValue create(boolean bool);

    /**
     * Create an empty JsonArray.
     *
     * @return a new JsonArray
     */
    JsonValue createArray();

    /**
     * Create a JsonNull.
     *
     * @return a JsonNull instance
     */
    JsonValue createNull();

    /**
     * Create an empty JsonObject.
     *
     * @return a new JsonObject
     */
    JsonValue createObject();

    /**
     * Parse a String in JSON format and return a JsonValue of the appropriate
     * type.
     *
     * @param jsonString a String in JSON format
     * @return a parsed JsonValue
     */
    JsonValue parse(String jsonString) throws JsonException;
}
