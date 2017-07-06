package org.activityinfo.model.form;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

/**
 * Set of updates to apply to a local copy of the database
 */
@JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
public final class FormSyncSet {

    private String formId;
    private String[] deleted;
    private UpdatedRecord[] updatedRecords;

    public FormSyncSet() {
    }

    @JsOverlay
    public static FormSyncSet create(String formId, String[] deleted, List<FormRecord> records) {
        FormSyncSet syncSet = new FormSyncSet();
        syncSet.formId = formId;
        syncSet.deleted = deleted;
        syncSet.updatedRecords = new UpdatedRecord[records.size()];
        for (int i = 0; i < syncSet.updatedRecords.length; i++) {
            syncSet.updatedRecords[i] = UpdatedRecord.create(records.get(i));
        }
        return syncSet;
    }

    @JsOverlay
    public static FormSyncSet emptySet(ResourceId formId) {
        FormSyncSet syncSet = new FormSyncSet();
        syncSet.formId = formId.asString();
        syncSet.deleted = new String[0];
        syncSet.updatedRecords = new UpdatedRecord[0];
        return syncSet;
    }

    @JsOverlay
    public static FormSyncSet create(ResourceId id, List<FormRecord> records) {
        return create(id.asString(), new String[0], records);
    }

    @JsOverlay
    public String getFormId() {
        return formId;
    }

    @JsOverlay
    public String[] getDeleted() {
        return deleted;
    }

    @JsOverlay
    public Iterable<UpdatedRecord> getUpdatedRecords() {
        return Arrays.asList(updatedRecords);
    }

    @JsOverlay
    public int getUpdatedRecordCount() {
        return updatedRecords.length;
    }

    @JsOverlay
    public boolean isEmpty() {
        return updatedRecords.length == 0 && deleted.length == 0;
    }

    @JsOverlay
    public UpdatedRecord getUpdatedRecord(int i) {
        return updatedRecords[i];
    }
}
