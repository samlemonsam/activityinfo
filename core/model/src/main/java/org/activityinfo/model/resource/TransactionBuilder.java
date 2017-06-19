package org.activityinfo.model.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 *  Constructs a set of updates to be applied atomically
 */
public class TransactionBuilder {
    
    private List<UpdateBuilder> updates = new ArrayList<>();

    public Iterable<UpdateBuilder> getUpdates() {
        return updates;
    }

    public UpdateBuilder create(ResourceId formId, ResourceId resourceId)  {
        UpdateBuilder update = new UpdateBuilder();
        update.setRecordId(resourceId);
        update.setFormId(formId);
        updates.add(update);
        return update;
    }

    public TransactionBuilder add(UpdateBuilder update) {
        updates.add(update);
        return this;
    }

    public TransactionBuilder add(Iterable<UpdateBuilder> updates) {
        for (UpdateBuilder update : updates) {
            this.updates.add(update);
        }
        return this;
    }

    public TransactionBuilder delete(ResourceId formId, ResourceId id) {
        UpdateBuilder update = new UpdateBuilder();
        update.setFormId(formId);
        update.setRecordId(id);
        update.delete();
        updates.add(update);
        return this;
    }

    public UpdateBuilder update(ResourceId formId, ResourceId id) {
        UpdateBuilder update = new UpdateBuilder();
        update.setFormId(formId);
        update.setRecordId(id);
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
