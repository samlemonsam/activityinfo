package org.activityinfo.model.form;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.JsonValue;


@JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
public final class UpdatedRecord {

    private String recordId;
    private String parentRecordId;
    private JsonValue fields;

    public UpdatedRecord() {
    }

    @JsOverlay
    public static UpdatedRecord create(FormRecord record) {
        UpdatedRecord update = new UpdatedRecord();
        update.parentRecordId = record.getParentRecordId();
        update.recordId = record.getRecordId();
        update.fields = record.getFields();
        return update;
    }

    @JsOverlay
    public String getRecordId() {
        return recordId;
    }

    @JsOverlay
    public String getParentRecordId() {
        return parentRecordId;
    }

    @JsOverlay
    public JsonValue getFields() {
        return fields;
    }
}
