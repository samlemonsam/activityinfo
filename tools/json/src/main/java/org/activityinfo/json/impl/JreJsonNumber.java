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
 * Server-side implementation of JsonNumber.
 */
public class JreJsonNumber extends JreJsonValue  {

    private static final long serialVersionUID = 1L;

    private transient double number;

    public JreJsonNumber(double number) {
        this.number = number;
    }

    @Override
    public boolean asBoolean() {
        return Double.isNaN(getNumber()) || Math.abs(getNumber()) == 0.0 ? false : true;
    }

    @Override
    public double asNumber() {
        return getNumber();
    }


    @Override
    public String asString() {
        return toJson();
    }

    @Override
    public boolean isJsonPrimitive() {
        return true;
    }

    public double getNumber() {
        return number;
    }


    public Object getObject() {
        return getNumber();
    }

    public JsonType getType() {
        return JsonType.NUMBER;
    }

    @Override
    public boolean jsEquals(JsonValue value) {
        return getObject().equals(((JreJsonValue) value).getObject());
    }

    @Override
    public void traverse(JsonVisitor visitor, JsonContext ctx) {
        visitor.visit(getNumber(), ctx);
    }

    public String toJson() {
        if (Double.isInfinite(number) || Double.isNaN(number)) {
            return "null";
        }
        String toReturn = String.valueOf(number);
        if (toReturn.endsWith(".0")) {
            toReturn = toReturn.substring(0, toReturn.length() - 2);
        }
        return toReturn;
    }

    @Override
    public long asLong() {
        return Long.parseLong(asString());
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        JreJsonNumber instance = parseJson(stream);
        this.number = instance.number;
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(toJson());
    }
}
