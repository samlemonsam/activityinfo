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
package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.store.hrd.FieldConverter;
import org.activityinfo.store.hrd.FieldConverters;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the current version of a FormRecord.
 * 
 * <p>Member of the Form Entity Group.</p>
 */
@Entity(name = "FormRecord")
public class FormRecordEntity {

    @Parent
    private Key<FormEntity> formKey;
    
    @Id
    private String id;
    
    /**
     * For sub form submissions, the parent form submission id. Indexed.
     */
    @Index
    private String parentRecordId;

    @Unindex
    private long version;
    
    @Unindex
    private long schemaVersion;

    @Index
    private int number;

    private EmbeddedEntity fieldValues;

    public FormRecordEntity() {
    }

    public FormRecordEntity(ResourceId formId, ResourceId recordId) {
        this.formKey = FormEntity.key(formId);
        this.id = recordId.asString();
    }

    public Key<FormRecordEntity> getKey() {
        return Key.create(this);
    }

    public ResourceId getFormId() {
        return ResourceId.valueOf(formKey.getName());
    }

    public ResourceId getRecordId() {
        return ResourceId.valueOf(id);
    }
    
    public String getParentRecordId() {
        return parentRecordId;
    }

    public void setParentRecordId(String parentRecordId) {
        this.parentRecordId = parentRecordId;
    }

    public void setParentRecordId(ResourceId recordId) {
        setParentRecordId(recordId.asString());
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public EmbeddedEntity getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(EmbeddedEntity fieldValues) {
        this.fieldValues = fieldValues;
    }

    public void setFieldValues(FormClass formClass, Map<ResourceId, FieldValue> values) {
        if(fieldValues == null) {
            fieldValues = new EmbeddedEntity();
        }
        for (FormField field : formClass.getFields()) {
            if(values.containsKey(field.getId())) {
                FieldValue value = values.get(field.getId());
                if(value == null) {
                    fieldValues.removeProperty(field.getName());
                } else {
                    FieldConverter converter = FieldConverters.forType(field.getType());
                    fieldValues.setUnindexedProperty(field.getName(), converter.toHrdProperty(value));
                }
            }
        }
    }


    public FormRecord toFormRecord(FormClass formClass) {
        FormRecord.Builder record = FormRecord.builder();
        record.setFormId(formClass.getId());
        record.setRecordId(getRecordId());

        if(formClass.getParentField().isPresent()) {
            record.setParentRecordId(ResourceId.valueOf(getParentRecordId()));
        }

        for (FormField formField : formClass.getFields()) {
            Object value = fieldValues.getProperty(formField.getName());
            if(value != null) {
                FieldConverter<?> converter = FieldConverters.forType(formField.getType());
                record.setFieldValue(formField.getId(), converter.toFieldValue(value));
            }
        }
        return record.build();    
    }
    
    public Map<ResourceId, FieldValue> toFieldValueMap(FormClass formClass) {
        Map<ResourceId, FieldValue> map = new HashMap<>();
        for (FormField formField : formClass.getFields()) {
            Object value = fieldValues.getProperty(formField.getName());
            if(value != null) {
                FieldConverter<?> converter = FieldConverters.forType(formField.getType());
                map.put(formField.getId(), converter.toFieldValue(value));
            }
        }
        return map;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public static Key<FormRecordEntity> key(ResourceId formId, ResourceId recordId) {
        Key<FormEntity> formKey = Key.create(FormEntity.class, formId.asString());
        Key<FormRecordEntity> recordKey = Key.create(formKey, FormRecordEntity.class, recordId.asString());
        return recordKey;
    }

    public static Key<FormRecordEntity> key(FormClass formClass, ResourceId recordId) {
        return key(formClass.getId(), recordId);
    }

    public int getRecordNumber() {
        return number;
    }

    public boolean hasRecordNumber() {
        return number != 0;
    }

    public void setRecordNumber(int number) {
        this.number = number;
    }

    public RecordRef getRecordRef() {
        return new RecordRef(getFormId(), getRecordId());
    }
}
