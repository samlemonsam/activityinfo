package org.activityinfo.model.form;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FormRecord {
    
    private String recordId;
    private String formId;
    private String parentRecordId;
    private JsonObject fields;

    public String getRecordId() {
        return recordId;
    }

    public String getFormId() {
        return formId;
    }

    public String getParentRecordId() {
        return parentRecordId;
    }

    public JsonObject getFields() {
        return fields;
    }

    public static FormRecord fromJson(String json) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        
        FormRecord formRecord = new FormRecord();
        formRecord.recordId = jsonObject.get("recordId").getAsString();
        formRecord.formId = jsonObject.get("formId").getAsString();
        
        if(jsonObject.has("parentRecordId")) {
            formRecord.parentRecordId = jsonObject.get("parentRecordId").getAsString();
        }

        formRecord.fields = jsonObject.get("fields").getAsJsonObject();

        return formRecord;
    }
}
