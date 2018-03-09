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

import org.activityinfo.json.JsonType;
import org.activityinfo.json.JsonValue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Server-side implementation of JsonString.
 */
public class JreJsonString extends JreJsonValue implements JsonValue {

    private static final long serialVersionUID = 1L;

    private transient String string;

    public JreJsonString(String string) {
        this.string = string;
    }

    @Override
    public boolean asBoolean() {
        return !getString().isEmpty();
    }

    @Override
    public double asNumber() {
        try {
            if (asString().isEmpty()) {
                return 0.0;
            } else {
                return Double.parseDouble(asString());
            }
        } catch (NumberFormatException nfe) {
            return Double.NaN;
        }
    }

    @Override
    public String asString() {
        return getString();
    }

    public Object getObject() {
        return getString();
    }

    public String getString() {
        return string;
    }

    public JsonType getType() {
        return JsonType.STRING;
    }

    @Override
    public boolean isJsonPrimitive() {
        return true;
    }

    @Override
    public boolean jsEquals(JsonValue value) {
        return getObject().equals(((JreJsonValue) value).getObject());
    }

    @Override
    public void traverse(JsonVisitor visitor, JsonContext ctx) {
        visitor.visit(getString(), ctx);
    }

    public String toJson() throws IllegalStateException {
        return JsonUtil.quote(getString());
    }

    @Override
    public boolean isString() {
        return true;
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        JreJsonString instance = parseJson(stream);
        this.string = instance.string;
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(toJson());
    }
}
