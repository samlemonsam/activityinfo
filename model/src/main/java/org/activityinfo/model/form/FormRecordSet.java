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
package org.activityinfo.model.form;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public class FormRecordSet implements JsonSerializable {
    private String formId;
    private List<FormRecord> records;

    private FormRecordSet() {
    }

    public FormRecordSet(String formId) {
        this.formId = formId;
    }

    public FormRecordSet(String formId, List<FormRecord> records) {
        this.formId = formId;
        this.records = records;
    }

    public FormRecordSet(ResourceId formId, List<FormRecord> records) {
        this(formId.asString(), records);
    }

    public String getFormId() {
        return formId;
    }

    public List<FormRecord> getRecords() {
        return records;
    }

    public static FormRecordSet fromJson(JsonValue jsonObject) {
        FormRecordSet model = new FormRecordSet();
        model.formId = JsonParsing.toNullableString(jsonObject.get("formId"));
        model.records = FormRecord.fromJsonArray(jsonObject.get("records"));
        return model;
    }

    @Override
    public JsonValue toJson() {

        JsonValue array = Json.createArray();
        for (FormRecord record : records) {
            array.add(record.toJson());
        }

        JsonValue object = Json.createObject();
        object.put("formId", formId);
        object.put("records", array);

        return object;
    }
}
