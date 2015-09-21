package org.activityinfo.geoadmin.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;

/**
 * Constructs a series of updates to a FormClass
 */
public class UpdateBuilder {
    
    private JsonObject update = new JsonObject();
    
    public UpdateBuilder setId(ResourceId id) {
        update.addProperty("@id", id.asString());
        return this;
    }
    
    public UpdateBuilder setClass(ResourceId id) {
        update.addProperty("@class", id.asString());
        return this;
    }
    
    public UpdateBuilder delete() {
        update.addProperty("@deleted", true);
        return this;
    }


    public void setProperty(ResourceId fieldId, FieldValue value) {
        if(value instanceof TextValue) {
            update.addProperty(fieldId.asString(), ((TextValue) value).asString());
        } else {
            throw new UnsupportedOperationException("value: " + value.getTypeClass());
        }
    }

    public JsonObject build() {
        return update;
    }
}
