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
package org.activityinfo.json;

import org.activityinfo.json.impl.JreJsonFactory;
import org.activityinfo.json.impl.JsonReflection;
import org.activityinfo.json.impl.JsonUtil;

/**
 * Vends out implementation of JsonFactory.
 */
public class Json {

    public static JsonValue createFromNullable(String string) {
        return instance().createFromNullable(string);
    }

    public static JsonValue create(String string) {
        return instance().create(string);
    }

    public static JsonValue create(boolean bool) {
        return instance().create(bool);
    }

    public static JsonValue createArray() {
        return instance().createArray();
    }

    public static JsonValue createNull() {
        return instance().createNull();
    }

    public static JsonValue create(double number) {
        return instance().create(number);
    }

    public static JsonValue createObject() {
        return instance().createObject();
    }

    public static JsonFactory instance() {
        return new JreJsonFactory();
    }

    public static JsonValue parse(String jsonString) {
        return instance().parse(jsonString);
    }

    public static String stringify(Object value) {
        return JsonUtil.stringify(Json.toJson(value));
    }

    public static String stringify(JsonValue jsonValue) {
        return JsonUtil.stringify(jsonValue);
    }

    public static String stringify(JsonValue jsonValue, int indent) {
        return JsonUtil.stringify(jsonValue, indent);
    }

    public static JsonValue toJson(Object value) {
        return JsonReflection.toJson(value);
    }

    public static <T> T fromJson(Class<T> clazz, JsonValue object) throws JsonMappingException {
        return JsonReflection.fromJson(clazz, object);
    }
}
