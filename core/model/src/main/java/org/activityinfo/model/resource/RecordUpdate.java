package org.activityinfo.model.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;

import javax.annotation.Nonnull;

/**
 * Constructs a series of updates to a FormClass
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class RecordUpdate {

    @JsonProperty(required = true)
    private String formId;

    @JsonProperty(required = true)
    private String recordId;

    private String parentRecordId;

    private boolean deleted;

    private JsonObject fields;

    @JsOverlay
    public ResourceId getFormId() {
        return ResourceId.valueOf(formId);
    }

    @JsOverlay
    public RecordUpdate setRecordId(ResourceId id) {
        this.recordId = id.asString();
        return this;
    }

    @JsOverlay
    public void setRecordId(String id) {
        this.recordId = id;
    }

    @JsOverlay
    public ResourceId getRecordId() {
        return ResourceId.valueOf(recordId);
    }

    @JsOverlay
    public void setFormId(String id) {
        this.formId = id;
    }

    @JsOverlay
    public RecordUpdate setFormId(ResourceId id) {
        this.formId = id.asString();
        return this;
    }


    @JsOverlay
    public boolean isDeleted() {
        return deleted;
    }


    @JsOverlay
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @JsOverlay
    public String getParentRecordId() {
        return parentRecordId;
    }

    @JsOverlay
    public void setParentRecordId(String parentRecordId) {
        this.parentRecordId = parentRecordId;
    }

    @JsOverlay
    public RecordUpdate delete() {
        this.deleted = true;
        return this;
    }

    @JsOverlay
    public void setFields(JsonObject object) {
        this.fields = object;
    }

    @JsOverlay
    public JsonObject getFields() {
        return fields;
    }

    @JsOverlay
    public void setFieldValue(ResourceId fieldId, FieldValue value) {
        if(value == null) {
            setFieldValue(fieldId.asString(), Json.createNull());
        } else {
            setFieldValue(fieldId.asString(), value.toJsonElement());
        }
    }

    @JsOverlay
    public void setFieldValue(String fieldId, String value) {
        setFieldValue(fieldId, Json.create(value));
    }

    @JsOverlay
    public void setFieldValue(String fieldId, double value) {
        setFieldValue(fieldId, Json.create(value));
    }

    @JsOverlay
    public void setFieldValue(String fieldId, JsonValue value) {
        if(fields == null) {
            fields = Json.createObject();
        }
        if(value == null) {
            fields.put(fieldId, Json.createNull());
        } else {
            fields.put(fieldId, value);
        }
    }

    @JsOverlay
    public RecordRef getRecordRef() {
        return new RecordRef(ResourceId.valueOf(formId), ResourceId.valueOf(recordId));
    }
}
