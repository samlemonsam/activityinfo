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

import java.io.Serializable;
import java.util.Map;

/**
 * Base interface for all Json values.
 */
public interface JsonValue extends Serializable {

    /**
     * Coerces underlying value to boolean according to the rules of Javascript coercion.
     */
    boolean asBoolean();

    /**
     * Coerces the underlying value to a number according to the rules of Javascript coercion.
     * If the value is a JsonNull or a JsonString that cannot be parsed as a number, then
     * Double.NaN will be returned.
     */
    double asNumber();

    int asInt();

    long asLong();


    /**
     * Coerces the underlying value to a String, or {@code null} for Json null values.son
     */
    String asString();

    /**
     * Returns an enumeration representing the fundamental JSON type.
     */
    JsonType getType();

    boolean isJsonNull();

    boolean isJsonArray();

    boolean isJsonPrimitive();

    boolean isJsonObject();

    boolean isNumber();

    boolean isString();

    boolean isBoolean();


    /**
     * Returns a serialized JSON string representing this value.
     *
     */
    String toJson();

    /**
     * Equivalent of Javascript '==' operator comparison between two values.
     */
    boolean jsEquals(JsonValue value);


    /**
     * If used in a GWT context (dev or prod mode), converts the object to a native JavaScriptObject
     * suitable for passing to JSNI methods. Otherwise, returns the current object in other contexts,
     * such as server-side use.
     */
    Object toNative();


    String toString();


    /**
     * Return the element (uncoerced) as a JsonValue or {@code Json.createNull()} if
     * this object has no value for the given key.
     *
     */
    JsonValue get(String key);


    /**
     * Return the element (uncoerced) as a boolean. If the type is not a boolean,
     * this can result in runtime errors.
     */
    boolean getBoolean(String key);

    /**
     * Return the element (uncoerced) as a number. If the type is not a number, this
     * can result in runtime errors.
     */
    double getNumber(String key);


    /**
     * Return the element (uncoerced) as a String. If the type is not a String, this
     * can result in runtime errors.
     */
    String getString(String key);

    /**
     * Set a given key to the given value.
     */
    void put(String key, JsonValue value);

    /**
     * Set a given key to the given String value.
     */
    void put(String key, String value);

    /**
     * Set a given key to the given double value.
     */
    void put(String key, double value);

    /**
     * Set a given key to the given boolean value.
     */
    void put(String key, boolean bool);

    /**
     * Test whether a given key has present.
     */
    boolean hasKey(String key);

    String[] keys();

    /**
     * Remove a given key and associated value from the object.
     *
     * @param key
     */
    void remove(String key);

    void add(String key, JsonValue value);


    /**
     * Return the ith element of the array (uncoerced) as a boolean. If the type is not a boolean,
     * this can result in runtime errors.
     */
    boolean getBoolean(int index);

    /**
     * Return the ith element of the array (uncoerced) as a number. If the type is not a number, this
     * can result in runtime errors.
     */
    double getNumber(int index);


    /**
     * Return the ith element of the array (uncoerced) as a String. If the type is not a String, this
     * can result in runtime errors.
     */
    String getString(int index);

    /**
     * Length of the array.
     */
    int length();

    /**
     * Set the value at index to be a given value.
     */
    void set(int index, JsonValue value);

    /**
     * Set the value at index to be a String value.
     */
    void set(int index, String string);

    /**
     * Set the value at index to be a number value.
     */
    void set(int index, double number);

    /**
     * Set the value at index to be a boolean value.
     */
    void set(int index, boolean bool);

    void add(JsonValue value);

    /**
     * Return the ith element of the array.
     */
    JsonValue get(int index);

    Iterable<JsonValue> values();

    Iterable<Map.Entry<String, JsonValue>> entrySet();
}
