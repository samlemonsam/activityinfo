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
