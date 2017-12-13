package org.activityinfo.store.spi;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes an update to a Resource
 */
public class TypedRecordUpdate {
    
    private long userId;
    private ResourceId formId;
    private ResourceId recordId;
    private ResourceId parentId;
    private boolean deleted = false;
    private Map<ResourceId, FieldValue> changedFieldValues = new HashMap<>();

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
