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
import org.activityinfo.json.JsonException;
import org.activityinfo.json.JsonFactory;
import org.activityinfo.json.JsonValue;

/**
 * JSNI based implementation of JsonFactory.
 */
public class JsJsonFactory implements JsonFactory {

    private static native <T extends JsonValue> T parse0(String jsonString) /*-{
        // assume Chrome, safe and non-broken JSON.parse impl
        var value = $wnd.JSON.parse(jsonString);
        // box for DevMode, not ProdMode
        return @com.google.gwt.core.client.GWT::isScript()() || value == null ? value : Object(value);
    }-*/;

    /*
     * MAGIC: String cast to object interface.
     */
    static native JsJsonValue createString(String string) /*-{
        return string;
    }-*/;

    /*
     * MAGIC: primitive boolean cast to object interface.
     */
    public static native JsJsonValue createBoolean(boolean bool) /*-{
        return bool;
    }-*/;

    /*
     * MAGIC: primitive number cast to object interface.
     */
    static native JsJsonValue createNumber(double number) /*-{
        return number;
    }-*/;


    @Override
    public JsonValue createArray() {
        return (JsJsonValue) JavaScriptObject.createArray();
    }

    public JsonValue create(String string) {
        return createString(string);
    }

    @Override
    public JsonValue createFromNullable(String string) {
        return createString(string);
    }

    public JsonValue create(double number) {
        return createNumber(number);
    }

    public JsonValue create(boolean bool) {
        return createBoolean(bool);
    }

    @Override
    public native JsonValue createNull() /*-{
        return null;
    }-*/;

    public JsJsonValue createObject() {
        return JavaScriptObject.createObject().cast();
    }

    public JsonValue parse(String jsonString) throws JsonException {
        try {
            return parse0(jsonString);
        } catch (Exception e) {
            throw new JsonException("Can't parse " + jsonString);
        }
    }
}
