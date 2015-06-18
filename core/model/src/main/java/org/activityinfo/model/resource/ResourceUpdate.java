package org.activityinfo.model.resource;

import org.activityinfo.model.type.FieldValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes an update to a Resource
 */
public class ResourceUpdate {
    
    private ResourceId resourceId;
    private boolean deleted = false;
    private Map<ResourceId, FieldValue> changedFieldValues = new HashMap<>();

    public void setResourceId(ResourceId resourceId) {
        this.resourceId = resourceId;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }
    
    public void set(ResourceId fieldId, FieldValue value) {
        changedFieldValues.put(fieldId, value);
    }

    public Map<ResourceId, FieldValue> getChangedFieldValues() {
        return changedFieldValues;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
