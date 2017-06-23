package org.activityinfo.model.resource;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.type.FieldValue;

import static org.activityinfo.json.Json.createObject;

/**
 * Constructs a series of updates to a FormClass
 */
public class UpdateBuilder {

    private JsonObject update = createObject();

    public ResourceId getFormId() {
        return ResourceId.valueOf(update.get("@class").asString());
    }

    public UpdateBuilder setRecordId(ResourceId id) {
        update.put("@id", id.asString());
        return this;
    }
    
    public UpdateBuilder setFormId(ResourceId id) {
        update.put("@class", id.asString());
        return this;
    }
    
    public UpdateBuilder delete() {
        update.put("@deleted", true);
        return this;
    }

    public void setProperty(ResourceId fieldId, FieldValue value) {
        if(value == null) {
            update.put(fieldId.asString(), Json.createNull());
        } else {
            update.put(fieldId.asString(), value.toJsonElement());
        }
    }

    public JsonObject build() {
        return update;
    }
}
