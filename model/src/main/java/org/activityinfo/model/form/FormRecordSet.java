package org.activityinfo.model.form;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public class FormRecordSet implements JsonSerializable {
    private String formId;
    private List<FormRecord> records;

    private FormRecordSet() {
    }

    public FormRecordSet(String formId) {
        this.formId = formId;
    }

    public FormRecordSet(String formId, List<FormRecord> records) {
        this.formId = formId;
        this.records = records;
    }

    public FormRecordSet(ResourceId formId, List<FormRecord> records) {
        this(formId.asString(), records);
    }

    public String getFormId() {
        return formId;
    }

    public List<FormRecord> getRecords() {
        return records;
    }

    public static FormRecordSet fromJson(JsonValue jsonObject) {
        FormRecordSet model = new FormRecordSet();
        model.formId = JsonParsing.toNullableString(jsonObject.get("formId"));
        model.records = FormRecord.fromJsonArray(jsonObject.get("records"));
        return model;
    }

    @Override
    public JsonValue toJson() {

        JsonValue array = Json.createArray();
        for (FormRecord record : records) {
            array.add(record.toJson());
        }

        JsonValue object = Json.createObject();
        object.put("formId", formId);
        object.put("records", array);

        return object;
    }
}
