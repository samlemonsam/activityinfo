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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import org.activityinfo.json.JsonType;
import org.activityinfo.json.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * JSO backed implementation of JsonValue.
 */
public final class JsJsonValue extends JavaScriptObject implements JsonValue {

    static native JsonValue box(JsonValue value) /*-{
        // box for DevMode, not ProdMode
        return @com.google.gwt.core.client.GWT::isScript()() || value == null ? value : Object(value);
    }-*/;

    static native JsonValue debox(JsonValue value) /*-{
        // we don't debox (currently), because ProdMode is now unboxed, and DevMode should stay boxed
        return value;
    }-*/;

    static native String getJsType(Object obj) /*-{
        return typeof obj;
    }-*/;

    static native boolean isArray(Object obj) /*-{
        // ensure that array detection works cross-frame
        return Object.prototype.toString.apply(@org.activityinfo.json.impl.JsJsonValue::debox(Lorg/activityinfo/json/JsonValue;)(obj)) === '[object Array]';
    }-*/;

    private static native boolean isNull(JsJsonValue jsJsonValue) /*-{
        // TODO(cromwellian): if this moves to GWT, we may have to support more leniency
        return jsJsonValue === null;
    }-*/;

    protected JsJsonValue() {
    }

    @Override
    final public native boolean asBoolean() /*-{
        return !!this;
    }-*/;

    @Override
    final public native double asNumber() /*-{
        if (this == null) {
            return 0;
        }
        return @com.google.gwt.core.client.GWT::isScript()() ?
            +@org.activityinfo.json.impl.JsJsonValue::debox(Lorg/activityinfo/json/JsonValue;)(this) :
            (+@org.activityinfo.json.impl.JsJsonValue::debox(Lorg/activityinfo/json/JsonValue;)(this)).valueOf();
    }-*/;

    @Override
    // avoid casts, as compiler will throw CCE trying to cast a raw JS String to an interface
    final public native String asString() /*-{
        return this == null ? null :
            ("" + @org.activityinfo.json.impl.JsJsonValue::debox(Lorg/activityinfo/json/JsonValue;)(this));
    }-*/;

    final public JsonType getType() {
        if (isJsonNull()) {
            return JsonType.NULL;
        }
        String jsType = getJsType(this);
        if ("string".equals(jsType)) {
            return JsonType.STRING;
        } else if ("number".equals(jsType)) {
            return JsonType.NUMBER;
        } else if ("boolean".equals(jsType)) {
            return JsonType.BOOLEAN;
        } else if ("object".equals(jsType)) {
            return isArray(this) ? JsonType.ARRAY : JsonType.OBJECT;
        }
        assert false : "Unknown Json Type";
        return null;
    }


    @Override
    final public native boolean jsEquals(JsonValue value) /*-{
        return @org.activityinfo.json.impl.JsJsonValue::debox(Lorg/activityinfo/json/JsonValue;)(this)
            === @org.activityinfo.json.impl.JsJsonValue::debox(Lorg/activityinfo/json/JsonValue;)(value);
    }-*/;

    final public native String toJson() /*-{
        // skip hashCode field
        return $wnd.JSON.stringify(this, function (keyName, value) {
            if (keyName == "$H") {
                return undefined; // skip hashCode property
            }
            return value;
        }, 0);
    }-*/;


    @Override
    public final int asInt() {
        return (int) asNumber();
    }

    @Override
    public final long asLong() {
        return Long.parseLong(asString());
    }

    @Override
    public final native boolean isString() /*-{
        return (typeof this) === "string";
    }-*/;

    @Override
    public final native boolean isJsonNull() /*-{
        return this == null;
    }-*/;

    @Override
    public final boolean isJsonArray() {
        return isArray(this);
    }


    @Override
    public final boolean isJsonPrimitive() {
        switch (getType()) {
            case STRING:
            case NUMBER:
            case BOOLEAN:
                return true;
        }
        return false;
    }

    @Override
    public final boolean isBoolean() {
        return getType() == JsonType.BOOLEAN;
    }

    @Override
    public final boolean isJsonObject() {
        return getType() == JsonType.OBJECT;
    }

    @Override
    public final boolean isNumber() {
        return getType() == JsonType.NUMBER;
    }

    final public native Object toNative() /*-{
        return @org.activityinfo.json.impl.JsJsonValue::debox(Lorg/activityinfo/json/JsonValue;)(this);
    }-*/;

    @Override
    public Iterable<Map.Entry<String, JsonValue>> entrySet() {
        Map<String, JsonValue> map = new HashMap<>();
        for (String key : keys()) {
            map.put(key, get(key));
        }
        return map.entrySet();
    }

    @Override
    public boolean getBoolean(String key) {
        return get(key).asBoolean();
    }


    public final native JsonValue get(int index) /*-{
        return this[index];
    }-*/;


    public final native JsonValue get(String key) /*-{
        return this[key];
    }-*/;


    @Override
    public Iterable<JsonValue> values() {
        return new JsonArrayIterable(this);
    }

    public boolean getBoolean(int index) {
        return get(index).asBoolean();
    }

    public double getNumber(int index) {
        return get(index).asNumber();
    }

    @Override
    public double getNumber(String key) {
        return get(key).asNumber();
    }

    @Override
    public String getString(String key) {
        return get(key).asString();
    }

    @Override
    public native void put(String key, JsonValue value) /*-{
        this[key] = value;
    }-*/;


    @Override
    public void put(String key, String value) {
        put(key, JsJsonFactory.createString(value));
    }

    @Override
    public void put(String key, double value) {
        put(key, JsJsonFactory.createNumber(value));
    }

    @Override
    public void put(String key, boolean bool) {
        put(key, JsJsonFactory.createBoolean(bool));
    }

    @Override
    public native boolean hasKey(String key) /*-{
        return key in this;
    }-*/;

    @Override
    public native void remove(String key) /*-{
        delete this[key];
    }-*/;

    @Override
    public void add(String key, JsonValue value) {
        put(key, value);
    }

    public String getString(int index) {
        return get(index).asString();
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


    private native String[] reinterpretCast(JsArrayString arrayString) /*-{
        return arrayString;
    }-*/;

    @Override
    public native int length() /*-{
        return this.length;
    }-*/;

    public native void set(int index, JsonValue value) /*-{
        this[index] = value;
    }-*/;


    public native void add(JsonValue value) /*-{
        this.push(value);
    }-*/;

    public void set(int index, String string) {
        set(index, JsJsonFactory.createString(string));
    }

    public void set(int index, double number) {
        set(index, JsJsonFactory.createNumber(number));
    }

    public void set(int index, boolean bool) {
        set(index, JsJsonFactory.createBoolean(bool));
    }

}
