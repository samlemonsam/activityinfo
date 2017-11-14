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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Server-side implementation of JsonBoolean.
 */
class JreJsonBoolean extends JreJsonValue {

    private static final long serialVersionUID = 1L;

    private transient boolean bool;

    public JreJsonBoolean(boolean bool) {
        this.bool = bool;
    }

    @Override
    public boolean asBoolean() {
        return getBoolean();
    }

    @Override
    public double asNumber() {
        return getBoolean() ? 1 : 0;
    }

    @Override
    public String asString() {
        return Boolean.toString(getBoolean());
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public boolean isJsonPrimitive() {
        return true;
    }

    public boolean getBoolean() {
        return bool;
    }

    public Object getObject() {
        return getBoolean();
    }

    public JsonType getType() {
        return JsonType.BOOLEAN;
    }

    @Override
    public boolean jsEquals(JsonValue value) {
        return getObject().equals(((JreJsonValue) value).getObject());
    }

    @Override
    public void traverse(JsonVisitor visitor, JsonContext ctx) {
        visitor.visit(getBoolean(), ctx);
    }

    public String toJson() throws IllegalStateException {
        return String.valueOf(bool);
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        JreJsonBoolean instance = parseJson(stream);
        this.bool = instance.bool;
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(toJson());
    }

}
