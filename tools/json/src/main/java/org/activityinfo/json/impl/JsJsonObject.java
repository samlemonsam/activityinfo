/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.activityinfo.json.impl;

import com.google.gwt.core.client.JsArrayString;
import org.activityinfo.json.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side implementation of JsonObject interface.
 */
final public class JsJsonObject extends JsJsonValue
        implements JsonObject {

    public static JsJsonObject create() {
        return createObject().cast();
    }

    protected JsJsonObject() {
    }

    public final native JsonValue get(String key) /*-{
        var value = this[key];
        // box for DevMode, not ProdMode
        return @com.google.gwt.core.client.GWT::isScript()() || value == null ? value : Object(value);
    }-*/;

    public JsonArray getArray(String key) {
        return get(key).getAsJsonArray();
    }

    public boolean getBoolean(String key) {
        return get(key).asBoolean();
    }

    public double getNumber(String key) {
        return get(key).asNumber();
    }

    public JsonObject getObject(String key) {
        return get(key).getAsJsonObject();
    }

    public String getString(String key) {
        return get(key).asString();
    }

    public native boolean hasKey(String key) /*-{
        return key in this;
    }-*/;

    @Override
    public Iterable<Map.Entry<String, JsonValue>> entrySet() {
        Map<String, JsonValue> map = new HashMap<>();
        for (String key : keys()) {
            map.put(key, get(key));
        }
        return map.entrySet();
    }

    public String[] keys() {
        JsArrayString str = keys0();
        return reinterpretCast(str);
    }

    public native JsArrayString keys0() /*-{
        var keys = [];
        for (var key in this) {
            if (Object.prototype.hasOwnProperty.call(this, key) && key != '$H') {
                keys.push(key);
            }
        }
        return keys;
    }-*/;

    public native void put(String key, JsonValue value) /*-{
        this[key] = value;
    }-*/;

    public void put(String key, String value) {
        put(key, JsJsonString.create(value));
    }

    public void put(String key, double value) {
        put(key, JsJsonNumber.create(value));
    }

    public void put(String key, boolean value) {
        put(key, JsJsonBoolean.create(value));
    }

    @Override
    public void add(String key, JsonValue value) {
        put(key, value);
    }

    /**
     * @deprecated use {@link #put(String, JsonValue)} instead.
     */
    @Deprecated
    public void put0(String key, JsonValue value) {
        put(key, value);
    }

    public native void remove(String key) /*-{
        delete this[key];
    }-*/;

    private native String[] reinterpretCast(JsArrayString arrayString) /*-{
        return arrayString;
    }-*/;
}