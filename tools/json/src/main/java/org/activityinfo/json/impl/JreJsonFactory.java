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

import org.activityinfo.json.JsonException;
import org.activityinfo.json.JsonFactory;
import org.activityinfo.json.JsonValue;


/**
 * Implementation of JsonFactory interface using org.json library.
 */
public class JreJsonFactory implements JsonFactory {

    public JsonValue create(String string) {
        assert string != null;
        return new JreJsonString(string);
    }

    @Override
    public JsonValue createFromNullable(String string) {
        if(string == null) {
            return createNull();
        } else {
            return create(string);
        }
    }

    public JsonValue create(double number) {
        return new JreJsonNumber(number);
    }

    public JsonValue create(boolean bool) {
        return new JreJsonBoolean(bool);
    }

    public JsonValue createArray() {
        return new JreJsonArray(this);
    }

    public JsonValue createNull() {
        return JreJsonNull.NULL_INSTANCE;
    }

    public JsonValue createObject() {
        return new JreJsonObject(this);
    }

    public JsonValue parse(String jsonString) throws JsonException {
        if (jsonString.startsWith("(") && jsonString.endsWith(")")) {
            // some clients send in (json) expecting an eval is required
            jsonString = jsonString.substring(1, jsonString.length() - 1);
        }
        return new JsonTokenizer(this, jsonString).nextValue();
    }
}
