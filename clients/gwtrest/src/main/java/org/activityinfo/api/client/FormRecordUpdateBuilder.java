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
package org.activityinfo.api.client;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

public class FormRecordUpdateBuilder {
    private JsonValue jsonObject = Json.createObject();

    private JsonValue fieldValues = Json.createObject();

    public FormRecordUpdateBuilder() {
        jsonObject.add("fieldValues", fieldValues);
    }

    public String toJsonString() {
        return jsonObject.toJson();
    }

    public JsonValue toJsonObject() {
        return jsonObject;
    }

    /**
     * Sets the deleted.
     *
     * @param deleted True if the record should be deleted
     */
    public FormRecordUpdateBuilder setDeleted(boolean deleted) {
        this.jsonObject.put("deleted", Json.create(deleted));
        return this;
    }

    public FormRecordUpdateBuilder setFieldValue(String name, String value) {
        fieldValues.put(name, value);
        return this;
    }

    public FormRecordUpdateBuilder setFieldValue(String name, Number value) {
        fieldValues.put(name, value.doubleValue());
        return this;
    }

    public FormRecordUpdateBuilder setFieldValue(String name, boolean value) {
        fieldValues.put(name, value);
        return this;
    }

    public FormRecordUpdateBuilder setFieldValue(String name, JsonValue value) {
        fieldValues.put(name, value);
        return this;
    }
}
