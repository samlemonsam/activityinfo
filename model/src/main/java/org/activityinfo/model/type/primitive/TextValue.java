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
package org.activityinfo.model.type.primitive;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

public class TextValue implements FieldValue, HasStringValue {

    private final String value;

    private TextValue(String value) {
        this.value = value;
    }

    /**
     * Returns a {@code TextValue} object, or {@code null} if
     * {@code value} is {@code null} or empty.
     */
    public static TextValue valueOf(String value) {
        if(value == null || value.isEmpty()) {
            return null;
        } else {
            return new TextValue(value.trim());
        }
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TextType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {
        return Json.create(value);
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TextValue textValue = (TextValue) o;

        if (!value.equals(textValue.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
