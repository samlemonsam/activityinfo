package org.activityinfo.model.resource;

import jsinterop.annotations.JsOverlay;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;

import java.util.ArrayList;
import java.util.List;

/**
 *  Constructs a set of updates to be applied atomically
 */
public class RecordTransactionBuilder {

    private List<RecordUpdate> updates = new ArrayList<>();

    public Iterable<RecordUpdate> getUpdates() {
        return updates;
    }



    public RecordUpdate create(ResourceId formId, ResourceId resourceId)  {
        RecordUpdate update = new RecordUpdate();
        update.setRecordId(resourceId);
        update.setFormId(formId);
        updates.add(update);
        return update;
    }

    public RecordTransactionBuilder create(FormInstance record) {
        RecordUpdate update = new RecordUpdate();
        update.setFormId(record.getFormId());
        update.setRecordId(record.getId());
        update.setFields(FormRecord.fromInstance(record).getFields());
        updates.add(update);
        return this;
    }

    public RecordTransactionBuilder add(RecordUpdate update) {
        updates.add(update);
        return this;
    }

    public RecordTransactionBuilder add(Iterable<RecordUpdate> updates) {
        for (RecordUpdate update : updates) {
            add(update);
        }
        return this;
    }

    public RecordTransactionBuilder delete(ResourceId formId, ResourceId id) {
        RecordUpdate update = new RecordUpdate();
        update.setFormId(formId);
        update.setRecordId(id);
        update.delete();
        add(update);
        return this;
    }

    public RecordUpdate update(ResourceId formId, ResourceId id) {
        RecordUpdate update = new RecordUpdate();
        update.setFormId(formId);
        update.setRecordId(id);
        updates.add(update);
        return update;
    }

    public RecordTransaction build() {
        RecordTransaction tx = new RecordTransaction();
        tx.changes = this.updates.toArray(new RecordUpdate[this.updates.size()]);
        return tx;
    }

    public JsonObject toJsonObject() {
        return Json.toJson(build()).getAsJsonObject();
    }

}
