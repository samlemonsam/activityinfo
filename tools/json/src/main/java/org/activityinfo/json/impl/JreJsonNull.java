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

import org.activityinfo.json.JsonType;
import org.activityinfo.json.JsonValue;

import java.io.ObjectStreamException;

/**
 * Server-side implementation of JsonObject.
 */
class JreJsonNull extends JreJsonValue {

    private static final long serialVersionUID = 1L;

    public static final JsonValue NULL_INSTANCE = new JreJsonNull();

    @Override
    public double asNumber() {
        return 0;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public String asString() {
        return null;
    }

    public Object getObject() {
        return null;
    }

    public JsonType getType() {
        return JsonType.NULL;
    }

    @Override
    public boolean jsEquals(JsonValue value) {
        return value == null || value.getType() == JsonType.NULL;
    }

    @Override
    public void traverse(JsonVisitor visitor, JsonContext ctx) {
        visitor.visitNull(ctx);
    }

    @Override
    public boolean isJsonNull() {
        return true;
    }

    public String toJson() {
        return null;
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private Object readResolve() throws ObjectStreamException {
        return NULL_INSTANCE;
    }
}
