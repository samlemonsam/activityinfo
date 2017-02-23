package org.activityinfo.geoadmin.model;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;

import java.util.ArrayList;
import java.util.List;

/**
 *  Constructs a set of updates to be applied atomically
 */
public class TransactionBuilder {
    
    private List<UpdateBuilder> updates = new ArrayList<>();
    
    public UpdateBuilder create(ResourceId collectionId, ResourceId resourceId)  {
        UpdateBuilder update = new UpdateBuilder();
        update.setId(resourceId);
        update.setClass(collectionId);
        updates.add(update);
        return update;
    }
    
    public TransactionBuilder delete(ResourceId id) {
        UpdateBuilder update = new UpdateBuilder();
        update.setId(id);
        update.delete();
        updates.add(update);
        return this;
    }

    public UpdateBuilder update(ResourceId id) {
        UpdateBuilder update = new UpdateBuilder();
        update.setId(id);
        updates.add(update);
        return update;
    }
    
    public JsonObject build() {
        JsonArray changes = new JsonArray();
        for (UpdateBuilder update : updates) {
            changes.add(update.build());
        }
        
        JsonObject object = new JsonObject();
        object.add("changes", changes);
        
        return object;
    }
}
