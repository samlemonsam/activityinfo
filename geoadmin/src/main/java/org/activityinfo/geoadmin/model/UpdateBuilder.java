package org.activityinfo.geoadmin.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoArea;
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

    public void setProperty(ResourceId fieldId, Extents extents) {
        GeoArea area = new GeoArea(extents);
        setProperty(fieldId, area);
    }

    public void setProperty(ResourceId fieldId, FieldValue value) {
        if(value instanceof TextValue) {
            update.addProperty(fieldId.asString(), ((TextValue) value).asString());
        } else if(value instanceof ReferenceValue) {
            update.addProperty(fieldId.asString(), ((ReferenceValue) value).getResourceId().asString());

        } else if(value instanceof IsRecord) {
            update.add(fieldId.asString(), Resources.toJsonObject(((IsRecord) value).asRecord()));
            
        } else {
            throw new UnsupportedOperationException("value: " + value.getTypeClass());
        }
    }

    public JsonObject build() {
        return update;
    }
}
