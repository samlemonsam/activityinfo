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
