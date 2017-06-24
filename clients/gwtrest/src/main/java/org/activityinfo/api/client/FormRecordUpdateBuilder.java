package org.activityinfo.api.client;


import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;

public class FormRecordUpdateBuilder {
    private JsonObject jsonObject = Json.createObject();

    private JsonObject fieldValues = Json.createObject();

    public FormRecordUpdateBuilder() {
        jsonObject.add("fieldValues", fieldValues);
    }

    public String toJsonString() {
        return jsonObject.toJson();
    }

    public JsonObject toJsonObject() {
        return jsonObject;
    }

    /**
     * Sets the deleted.
     *
     * @param deleted True if the record should be deleted
     */
    public FormRecordUpdateBuilder setDeleted(boolean deleted) {
        this.jsonObject.put("deleted", Json.create(deleted));
        return this;
    }

    public FormRecordUpdateBuilder setFieldValue(String name, String value) {
        fieldValues.put(name, value);
        return this;
    }

    public FormRecordUpdateBuilder setFieldValue(String name, Number value) {
        fieldValues.put(name, value.doubleValue());
        return this;
    }

    public FormRecordUpdateBuilder setFieldValue(String name, boolean value) {
        fieldValues.put(name, value);
        return this;
    }

    public FormRecordUpdateBuilder setFieldValue(String name, JsonValue value) {
        fieldValues.put(name, value);
        return this;
    }
}
