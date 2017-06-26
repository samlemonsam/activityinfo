package org.activityinfo.ui.client.store.offline;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.type.RecordRef;

/**
 * The value of the IndexedDB record ObjectStore
 *
 *
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class RecordObject {

    private String parentFormId;
    private JsonObject fields;


    @JsOverlay
    public static RecordObject from(FormRecord record) {
        RecordObject object = new RecordObject();
        object.parentFormId = record.getParentRecordId();
        object.fields = record.getFields();
        return object;
    }

    @JsOverlay
    public String getParentFormId() {
        return parentFormId;
    }

    @JsOverlay
    public void setParentFormId(String parentFormId) {
        this.parentFormId = parentFormId;
    }

    @JsOverlay
    public JsonObject getFields() {
        return fields;
    }

    @JsOverlay
    public void setFields(JsonObject fields) {
        this.fields = fields;
    }

    @JsOverlay
    public void setField(String fieldName, JsonValue value) {
        if(fields == null) {
            fields = Json.createObject();
        }
        fields.put(fieldName, value);
    }

    @JsOverlay
    public JsonValue getField(String fieldName) {
        return fields.get(fieldName);
    }

    @JsOverlay
    public FormRecord toFormRecord(RecordRef recordRef) {
        return new FormRecord(recordRef, parentFormId, fields);
    }
}
