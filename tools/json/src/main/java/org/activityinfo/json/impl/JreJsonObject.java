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

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonFactory;
import org.activityinfo.json.JsonType;
import org.activityinfo.json.JsonValue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Server-side implementation of JsonObject.
 */
public class JreJsonObject extends JreJsonValue implements JsonValue {

    private static final long serialVersionUID = 1L;

    private static List<String> stringifyOrder(String[] keys) {
        List<String> toReturn = new ArrayList<String>();
        List<String> nonNumeric = new ArrayList<String>();
        for (String key : keys) {
            if (key.matches("\\d+")) {
                toReturn.add(key);
            } else {
                nonNumeric.add(key);
            }
        }
        Collections.sort(toReturn);
        toReturn.addAll(nonNumeric);
        return toReturn;
    }

    private transient JsonFactory factory;
    private transient Map<String, JsonValue> map = new LinkedHashMap<String, JsonValue>();

    public JreJsonObject(JsonFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public double asNumber() {
        return Double.NaN;
    }

    @Override
    public String asString() {
        return "[object Object]";
    }

    public JsonValue get(String key) {
        JsonValue value = map.get(key);
        if(value == null) {
            return JreJsonNull.NULL_INSTANCE;
        }
        return value;
    }

    public boolean getBoolean(String key) {
        return get(key).asBoolean();
    }

    public double getNumber(String key) {
        return get(key).asNumber();
    }

    public Object getObject() {
        Map<String, Object> obj = new HashMap<String, Object>();
        for (Map.Entry<String, JsonValue> e : map.entrySet()) {
            obj.put(e.getKey(), ((JreJsonValue) e.getValue()).getObject());
        }
        return obj;
    }


    public String getString(String key) {
        return get(key).asString();
    }

    public JsonType getType() {
        return JsonType.OBJECT;
    }

    @Override
    public boolean hasKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public boolean jsEquals(JsonValue value) {
        return getObject().equals(((JreJsonValue) value).getObject());
    }

    @Override
    public String[] keys() {
        return map.keySet().toArray(new String[map.size()]);
    }


    public void put(String key, JsonValue value) {
        if (value == null) {
            value = factory.createNull();
        }
        map.put(key, value);
    }

    public void put(String key, String value) {
        put(key, factory.createFromNullable(value));
    }

    public void put(String key, double value) {
        put(key, factory.create(value));
    }

    public void put(String key, boolean bool) {
        put(key, factory.create(bool));
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public void add(String key, JsonValue value) {
        put(key, value);
    }

    public void set(String key, JsonValue value) {
        put(key, value);
    }

    public String toJson() {
        return JsonUtil.stringify(this);
    }

    public String toString() {
        throw new UnsupportedOperationException("SHOULD NOT BE CALLED!!");
    }

    @Override
    public Iterable<Map.Entry<String, JsonValue>> entrySet() {
        return map.entrySet();
    }

    @Override
    public void traverse(JsonVisitor visitor, JsonContext ctx) {
        if (visitor.visitObject(this, ctx)) {
            JsonObjectContext objCtx = new JsonObjectContext(this);
            for (String key : stringifyOrder(keys())) {
                objCtx.setCurrentKey(key);
                if (visitor.visitKey(objCtx.getCurrentKey(), objCtx)) {
                    visitor.accept(get(key), objCtx);
                    objCtx.setFirst(false);
                }
            }
        }
        visitor.endObjectVisit(this, ctx);
    }

    @Override
    public boolean isJsonObject() {
        return true;
    }


    @com.google.gwt.core.shared.GwtIncompatible
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        JreJsonObject instance = parseJson(stream);
        this.factory = Json.instance();
        this.map = instance.map;
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(toJson());
    }
}
