/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.model.form;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.activityinfo.json.JsonParser;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.activityinfo.json.Json.createObject;

public class FormRecord implements JsonSerializable {
    
    private String recordId;
    private String formId;
    private String parentRecordId;
    private JsonValue fields;

    private FormRecord() {
    }

    public FormRecord(RecordRef ref, String parentRecordId, JsonValue fields) {
        this.formId = ref.getFormId().asString();
        this.recordId = ref.getRecordId().asString();
        this.parentRecordId = parentRecordId;
        this.fields = fields;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getFormId() {
        return formId;
    }

    @Schema(hidden = true)
    public RecordRef getRef() {
        return new RecordRef(ResourceId.valueOf(formId), ResourceId.valueOf(recordId));
    }

    public String getParentRecordId() {
        return parentRecordId;
    }

    @ArraySchema(schema = @Schema(ref = "#/components/schemas/FieldValue"))
    public JsonValue getFields() {
        return fields;
    }


    public static FormRecord fromJson(String json) {
        JsonParser parser = new org.activityinfo.json.JsonParser();
        return fromJson(parser.parse(json));
    }
    
    public static FormRecord fromJson(JsonValue element) {
        JsonValue jsonObject = element;

        FormRecord formRecord = new FormRecord();
        formRecord.recordId = jsonObject.get("recordId").asString();
        formRecord.formId = jsonObject.get("formId").asString();

        if(jsonObject.hasKey("parentRecordId")) {
            formRecord.parentRecordId = jsonObject.get("parentRecordId").asString();
        }

        formRecord.fields = jsonObject.get("fields");

        return formRecord;
    }

    @Override
    public JsonValue toJson() {
        
        assert recordId != null;
        assert formId != null;

        JsonValue jsonObject = createObject();
        jsonObject.put("recordId", recordId);
        jsonObject.put("formId", formId);
        if(parentRecordId != null) {
            jsonObject.put("parentRecordId", parentRecordId);
        }
        jsonObject.put("fields", fields);
        return jsonObject;
    }
    
    public static List<FormRecord> fromJsonArray(JsonValue array) {
        List<FormRecord> list = new ArrayList<>();
        for (JsonValue jsonElement : array.values()) {
            list.add(fromJson(jsonElement));
        }
        return list;
    }

    public static FormRecord fromInstance(FormInstance instance) {
        FormRecord record = new FormRecord();
        record.recordId = instance.getId().asString();
        record.formId = instance.getFormId().asString();
        record.fields = createObject();

        for (Map.Entry<ResourceId, FieldValue> entry : instance.getFieldValueMap().entrySet()) {
            String field = entry.getKey().asString();
            if(!field.equals("classId")) {
                if(entry.getValue() != null) {
                    record.fields.put(field, entry.getValue().toJson());
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
            record.fields = createObject();
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
                this.record.fields.put(fieldId.asString(), value);
            }
            return this;
        }
        
        public Builder setFieldValue(ResourceId fieldId, FieldValue value) {
            if(value == null) {
                this.record.fields.remove(fieldId.asString());
            } else {
                this.record.fields.put(fieldId.asString(), value.toJson());
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
