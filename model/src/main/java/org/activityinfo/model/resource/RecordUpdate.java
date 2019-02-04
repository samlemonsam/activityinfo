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
package org.activityinfo.model.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;

import java.util.Optional;

/**
 * An update to specific {@code Record}
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class RecordUpdate {

    @JsonProperty(required = true)
    private String formId;

    @JsonProperty(required = true)
    private String recordId;

    private String parentRecordId;

    private boolean deleted;

    private JsonValue fields;

    @JsOverlay
    public ResourceId getFormId() {
        return ResourceId.valueOf(formId);
    }

    @JsOverlay
    public RecordUpdate setRecordId(ResourceId id) {
        this.recordId = id.asString();
        return this;
    }

    @JsOverlay
    public void setRecordId(String id) {
        this.recordId = id;
    }

    @JsOverlay
    public ResourceId getRecordId() {
        return ResourceId.valueOf(recordId);
    }

    @JsOverlay
    public void setFormId(String id) {
        this.formId = id;
    }

    @JsOverlay
    public RecordUpdate setFormId(ResourceId id) {
        this.formId = id.asString();
        return this;
    }


    @JsOverlay
    public boolean isDeleted() {
        return deleted;
    }


    @JsOverlay
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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
    public void setParentRecordId(Optional<String> parentRecordId) {
        this.parentRecordId = parentRecordId.orElse(null);
    }

    @JsOverlay
    public RecordUpdate delete() {
        this.deleted = true;
        return this;
    }

    @JsOverlay
    public void setFields(JsonValue object) {
        this.fields = object;
    }

    @JsOverlay
    public JsonValue getFields() {
        return fields;
    }

    @JsOverlay
    public void setFieldValue(ResourceId fieldId, FieldValue value) {
        if(value == null) {
            setFieldValue(fieldId.asString(), Json.createNull());
        } else {
            setFieldValue(fieldId.asString(), value.toJson());
        }
    }

    @JsOverlay
    public void setFieldValue(String fieldId, String value) {
        setFieldValue(fieldId, Json.create(value));
    }

    @JsOverlay
    public void setFieldValue(String fieldId, double value) {
        setFieldValue(fieldId, Json.create(value));
    }

    @JsOverlay
    public void setFieldValue(String fieldId, JsonValue value) {
        if(fields == null) {
            fields = Json.createObject();
        }
        fields.put(fieldId, value);
    }

    @JsOverlay
    public RecordRef getRecordRef() {
        return new RecordRef(ResourceId.valueOf(formId), ResourceId.valueOf(recordId));
    }
}
