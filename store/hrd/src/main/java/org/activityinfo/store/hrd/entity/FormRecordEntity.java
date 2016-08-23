package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
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
    
    private boolean deleted;

    @Unindex
    private long version;
    
    @Unindex
    private long schemaVersion;
    
    private EmbeddedEntity fieldValues;

    public FormRecordEntity() {
    }

    public FormRecordEntity(ResourceId formId, ResourceId recordId) {
        this.formKey = FormEntity.key(formId);
        if(recordId.getDomain() == ResourceId.GENERATED_ID_DOMAIN) {
            this.id = localId(formId, recordId);
        } else {
            this.id = recordId.asString();
        }
    }

    private String localId(ResourceId formId, ResourceId recordId) {
        String id = recordId.asString();
        if(!id.startsWith(formId.asString())) {
            throw new IllegalStateException("recordId does not start with " + formId);
        }
        return id.substring(formId.asString().length() + 1);
    }
    
    public ResourceId getFormId() {
        return ResourceId.valueOf(formKey.getName());
    }

    public ResourceId getRecordId() {
        ResourceId formId = getFormId();
        if(formId.getDomain() == ResourceId.GENERATED_ID_DOMAIN) {
            return ResourceId.valueOf(formKey.getName() + "-" + id);
        } else {
            return ResourceId.valueOf(id);
        }
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

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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
        record.setRecordId(ResourceId.valueOf(id));

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

    public boolean isDeleted() {
        return deleted;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public static Key<FormRecordEntity> key(ResourceId recordId) {
        ResourceId.checkSubmissionId(recordId);


        String[] parts = recordId.asString().split("-", 2);
        String recordLocalId = recordId.asString().substring(recordId.asString().indexOf("-") + 1);

        Key<FormEntity> formKey = Key.create(FormEntity.class, parts[0]);
        Key<FormRecordEntity> recordKey = Key.create(formKey, FormRecordEntity.class, recordLocalId);

        return recordKey;
    }

    public static Key<FormRecordEntity> key(ResourceId formId, ResourceId recordId) {
        if(recordId.getDomain() == ResourceId.GENERATED_ID_DOMAIN) {
            return key(recordId);
        } else {
            Key<FormEntity> formKey = Key.create(FormEntity.class, formId.asString());
            Key<FormRecordEntity> recordKey = Key.create(formKey, FormRecordEntity.class, recordId.asString());
            return recordKey;
        }
    }
    
    public static Key<FormRecordEntity> key(FormClass formClass, ResourceId recordId) {
        return key(formClass.getId(), recordId);
    }
}
