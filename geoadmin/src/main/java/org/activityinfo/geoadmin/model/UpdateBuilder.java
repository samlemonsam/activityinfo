package org.activityinfo.geoadmin.model;

import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;

/**
 * Constructs a series of updates to a FormClass
 */
public class UpdateBuilder {
    
    private JsonObject update;
    
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
    
    
}
