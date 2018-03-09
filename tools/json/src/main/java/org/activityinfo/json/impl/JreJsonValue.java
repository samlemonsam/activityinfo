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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import org.activityinfo.json.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Map;

/**
 * JRE (non-Client) implementation of JreJsonValue.
 */
public abstract class JreJsonValue implements JsonValue {


    @Override
    public boolean isNumber() {
        return getType() == JsonType.NUMBER;
    }

    @GwtIncompatible
    protected static <T extends JreJsonValue> T parseJson(ObjectInputStream stream)
            throws ClassNotFoundException, IOException {
        String jsonString = (String) stream.readObject();
        return (T) Json.instance().parse(jsonString);
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public boolean asBoolean() {
        throw conversionException(JsonType.BOOLEAN);
    }

    @Override
    public double asNumber() {
        throw conversionException(JsonType.NUMBER);
    }

    @Override
    public final int asInt() {
        return (int)asNumber();
    }

    @Override
    public long asLong() {
        throw conversionException(JsonType.NUMBER);
    }

    @Override
    public String asString() {
        throw conversionException(JsonType.STRING);
    }


    @Override
    public boolean isJsonNull() {
        return false;
    }

    @Override
    public boolean isJsonArray() {
        return false;
    }

    @Override
    public boolean isJsonPrimitive() {
        return false;
    }

    @Override
    public boolean isJsonObject() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean jsEquals(JsonValue value) {
        throw new UnsupportedOperationException("TODO");
    }


    @Override
    public boolean getBoolean(String key) {
        throw conversionException(JsonType.OBJECT);
    }

    @Override
    public double getNumber(String key) {
        throw conversionException(JsonType.OBJECT);
    }

    @Override
    public JsonValue get(String key) {
        throw conversionException(JsonType.OBJECT);
    }

    @Override
    public String getString(String key) {
        throw conversionException(JsonType.OBJECT);
    }

    @Override
    public void put(String key, JsonValue value) {
        throw conversionException(JsonType.OBJECT);
    }

    @Override
    public void put(String key, String value) {
        throw conversionException(JsonType.OBJECT);
    }

    @Override
    public void put(String key, double value) {
        throw conversionException(JsonType.OBJECT);
    }

    @Override
    public void put(String key, boolean bool) {
        throw conversionException(JsonType.OBJECT);
    }

    @Override
    public boolean hasKey(String key) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String[] keys() {
        return new String[0];
    }

    @Override
    public void remove(String key) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void add(String key, JsonValue value) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean getBoolean(int index) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getNumber(int index) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public JsonValue get(int index) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String getString(int index) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int length() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void set(int index, JsonValue value) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void set(int index, String string) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void set(int index, double number) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void set(int index, boolean bool) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void add(JsonValue value) {
        throw new UnsupportedOperationException("TODO");
    }

    public abstract Object getObject();

    @Override
    public Iterable<JsonValue> values() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<Map.Entry<String, JsonValue>> entrySet() {
        return Collections.emptyList();
    }

    public abstract void traverse(JsonVisitor visitor, JsonContext ctx);




    private JsonException conversionException(JsonType toType) {
        return new JsonException("Cannot convert " + getType().name() + " to " + toType);
    }

    @Override
    public Object toNative() {
        if(GWT.isClient()) {
            return JsonUtils.safeEval(this.toJson());
        } else {
            throw new UnsupportedOperationException("toNative not supported on the server");
        }
    }
}
