package org.activityinfo.model.form;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormRecord {
    
    private String recordId;
    private String formId;
    private String parentRecordId;
    private JsonObject fields;

    private FormRecord() {
    }
    
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
        return fromJson(parser.parse(json));
    }
    
    public static FormRecord fromJson(JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();

        FormRecord formRecord = new FormRecord();
        formRecord.recordId = jsonObject.get("recordId").getAsString();
        formRecord.formId = jsonObject.get("formId").getAsString();

        if(jsonObject.has("parentRecordId")) {
            formRecord.parentRecordId = jsonObject.get("parentRecordId").getAsString();
        }

        formRecord.fields = jsonObject.get("fields").getAsJsonObject();

        return formRecord;
    }
    
    public JsonObject toJsonElement() {
        
        assert recordId != null;
        assert formId != null;
        
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("recordId", recordId);
        jsonObject.addProperty("formId", formId);
        if(parentRecordId != null) {
            jsonObject.addProperty("parentRecordId", parentRecordId);
        }
        jsonObject.add("fields", fields);
        return jsonObject;
    }
    
    public static List<FormRecord> fromJsonArray(JsonArray array) {
        List<FormRecord> list = new ArrayList<>();
        for (JsonElement jsonElement : array) {
            list.add(fromJson(jsonElement));
        }
        return list;
    }

    public static FormRecord fromInstance(FormInstance instance) {
        FormRecord record = new FormRecord();
        record.recordId = instance.getId().asString();
        record.formId = instance.getFormId().asString();
        record.fields = new JsonObject();

        for (Map.Entry<ResourceId, FieldValue> entry : instance.getFieldValueMap().entrySet()) {
            String field = entry.getKey().asString();
            if(!field.equals("classId")) {
                if(entry.getValue() != null) {
                    record.fields.add(field, entry.getValue().toJsonElement());
                }
            }
        }
        return record;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        
        private FormRecord record;

        public Builder() {
            record = new FormRecord();
            record.fields = new JsonObject();
        }

        public Builder setRecordId(ResourceId id) {
            this.record.recordId = id.asString();
            return this;
        }
        
        public Builder setFormId(ResourceId id) {
            this.record.formId = id.asString();
            return this;
        }
        
        public Builder setParentRecordId(ResourceId id) {
            this.record.parentRecordId = id.asString();
            return this;
        }
        
        public Builder setFieldValue(ResourceId fieldId, String value) {
            if(value == null) {
                this.record.fields.remove(fieldId.asString());
            } else {
                this.record.fields.addProperty(fieldId.asString(), value);
            }
            return this;
        }
        
        public Builder setFieldValue(ResourceId fieldId, FieldValue value) {
            if(value == null) {
                this.record.fields.remove(fieldId.asString());
            } else {
                this.record.fields.add(fieldId.asString(), value.toJsonElement());
            }
            return this;
        }
        
        public FormRecord build() {
            assert record.recordId != null;
            assert record.formId != null;
            
            return record;
        }

    }
    
}
