/*
 * Copyright 2011 Google Inc.
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

import org.activityinfo.json.*;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * JRE (non-Client) implementation of JreJsonValue.
 */
public abstract class JreJsonValue implements JsonValue {
    public abstract Object getObject();

    public abstract void traverse(JsonVisitor visitor, JsonContext ctx);

    @Override
    public Object toNative() {
        return this;
    }

    protected static <T extends JsonValue> T parseJson(ObjectInputStream stream)
            throws ClassNotFoundException, IOException {
        String jsonString = (String) stream.readObject();
        return Json.instance().parse(jsonString);
    }

    @Override
    public JsonObject getAsJsonObject() {
        return ((JsonObject) this);
    }

    @Override
    public JsonArray getAsJsonArray() {
        return ((JsonArray) this);
    }

    @Override
    public double asNumber() {
        return asNumber();
    }

    @Override
    public String asString() {
        return asString();
    }

    @Override
    public long asLong() {
        return (long) asNumber();
    }

    @Override
    public boolean isString() {
        return getType() == JsonType.STRING;
    }

    @Override
    public boolean asBoolean() {
        return asBoolean();
    }

    @Override
    public boolean isBoolean() {
        return getType() == JsonType.BOOLEAN;
    }

    @Override
    public int asInt() {
        return (int)asNumber();
    }

    @Override
    public boolean isJsonNull() {
        return getType() == JsonType.NULL;
    }

    @Override
    public boolean isJsonArray() {
        return getType() == JsonType.ARRAY;
    }

    @Override
    public boolean isJsonString() {
        return getType() == JsonType.STRING;
    }

    @Override
    public boolean isJsonPrimitive() {
        switch (getType()) {
            case STRING:
            case NUMBER:
            case BOOLEAN:
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean isJsonObject() {
        return getType() == JsonType.OBJECT;
    }



    @Override
    public boolean isNumber() {
        return getType() == JsonType.NUMBER;
    }

    @Override
    public String toString() {
        return toJson();
    }
}
