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
package org.activityinfo.store.spi;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes an update to a Record
 */
public class TypedRecordUpdate {
    
    private long userId;
    private ResourceId formId;
    private ResourceId recordId;
    private ResourceId parentId;
    private boolean deleted = false;
    private Map<ResourceId, FieldValue> changedFieldValues = new HashMap<>();

    public TypedRecordUpdate() {
    }

    public TypedRecordUpdate(long userId, FormInstance record) {
        this.userId = userId;
        this.formId = record.getFormId();
        this.recordId = record.getRef().getRecordId();
        this.parentId = record.getParentRecordId();
        this.changedFieldValues.putAll(record.getFieldValueMap());
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setRecordId(ResourceId recordId) {
        this.recordId = recordId;
    }
    
    public ResourceId getRecordId() {
        return recordId;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public void setFormId(ResourceId formId) {
        this.formId = formId;
    }

    public ResourceId getParentId() {
        return parentId;
    }

    public void setParentId(ResourceId parentId) {
        this.parentId = parentId;
    }

    public void set(ResourceId fieldId, FieldValue value) {
        changedFieldValues.put(fieldId, value);
    }

    public Map<ResourceId, FieldValue> getChangedFieldValues() {
        return changedFieldValues;
    }

    public JsonValue getChangedFieldValuesObject() {
        JsonValue object = Json.createObject();
        for (Map.Entry<ResourceId, FieldValue> entry : changedFieldValues.entrySet()) {
            object.put(entry.getKey().asString(), entry.getValue().toJson());
        }
        return object;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
