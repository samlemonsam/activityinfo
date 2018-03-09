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
package org.activityinfo.ui.client.store.offline;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.UpdatedRecord;
import org.activityinfo.model.type.RecordRef;

/**
 * The value of the IndexedDB record ObjectStore
 *
 *
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class RecordObject {

    private String parentRecordId;
    private JsonValue fields;


    @JsOverlay
    public static RecordObject from(FormRecord record) {
        RecordObject object = new RecordObject();
        object.parentRecordId = record.getParentRecordId();
        object.fields = record.getFields();
        return object;
    }

    @JsOverlay
    public static RecordObject from(UpdatedRecord record) {
        RecordObject object = new RecordObject();
        object.parentRecordId = record.getParentRecordId();
        object.fields = record.getFields();
        return object;
    }

    @JsOverlay
    public String getParentRecordId() {
        return parentRecordId;
    }

    @JsOverlay
    public void setParentRecordId(String parentRecordId) {
        this.parentRecordId = parentRecordId;
    }

    @JsOverlay
    public JsonValue getFields() {
        return fields;
    }

    @JsOverlay
    public void setFields(JsonValue fields) {
        this.fields = fields;
    }

    @JsOverlay
    public void setField(String fieldName, JsonValue value) {
        if(fields == null) {
            fields = Json.createObject();
        }
        fields.put(fieldName, value);
    }

    @JsOverlay
    public JsonValue getField(String fieldName) {
        return fields.get(fieldName);
    }

    @JsOverlay
    public FormRecord toFormRecord(RecordRef recordRef) {
        return new FormRecord(recordRef, parentRecordId, fields);
    }

}
