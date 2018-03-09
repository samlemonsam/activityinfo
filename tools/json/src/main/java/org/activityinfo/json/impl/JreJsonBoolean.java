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
