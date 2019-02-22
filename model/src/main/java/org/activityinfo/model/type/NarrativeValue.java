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
package org.activityinfo.model.type;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.primitive.HasStringValue;

public class NarrativeValue implements FieldValue, HasStringValue {

    private String text;

    private NarrativeValue(String text) {
        this.text = text;
    }

    public static NarrativeValue valueOf(String value) {
        if(value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        if(trimmedValue.isEmpty()) {
            return null;
        }
        return new NarrativeValue(trimmedValue);
    }

    public String getText() {
        return text;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return NarrativeType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {
        return Json.create(text);
    }


    @Override
    public String asString() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NarrativeValue that = (NarrativeValue) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }

}
