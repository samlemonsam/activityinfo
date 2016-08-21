package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
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
 * <p>Member of the Form entity group.</p>
 */
public class FormRecordEntity implements TypedEntity {
    
    public static final String KIND = "FormRecord";

    /**
     * For sub form submissions, the parent form submission id. Indexed.
     */
    public static final String PARENT_PROPERTY = "@parent";

    public static final String DELETED = "@deleted";
    
    public static final String VERSION = "@version";

    public static final String SCHEMA_VERSION = "@schemaVersion";


    private FormRecordKey key;
    private final Entity entity;

    public FormRecordEntity(FormRecordKey key) {
        this.key = key;
        this.entity = new Entity(key.raw());
    }

    public FormRecordEntity(Entity entity) {
        this.key = new FormRecordKey(entity.getKey());
        this.entity = entity;
    }
    
    public FormRecord toFormRecord(FormClass formClass) {
        FormRecord.Builder record = FormRecord.builder();
        record.setFormId(formClass.getId());
        record.setRecordId(key.getRecordId());

        if(formClass.getParentField().isPresent()) {
            record.setParentRecordId(getParentId());
        }

        for (FormField formField : formClass.getFields()) {
            Object value = entity.getProperty(formField.getName());
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
            Object value = entity.getProperty(formField.getName());
            if(value != null) {
                FieldConverter<?> converter = FieldConverters.forType(formField.getType());
                map.put(formField.getId(), converter.toFieldValue(value));
            }
        }
        return map;
    }

    public FormRecordKey getKey() {
        return key;
    }

    public ResourceId getParentId() {
        String parentId = (String) entity.getProperty(PARENT_PROPERTY);
        if(parentId == null) {
            return null;
        } else {
            return ResourceId.valueOf(parentId);
        }
    }

    public boolean getDeleted() {
        Boolean deleted = (Boolean) entity.getProperty(DELETED);
        if(deleted == null) {
            return false;
        } else {
            return deleted;
        }
    }
    
    public void setVersion(long version) {
        entity.setProperty(VERSION, version);
    }


    public long getVersion() {
        return (Long)entity.getProperty(VERSION);
    }
    
    public void setSchemaVersion(long version) {
        entity.setUnindexedProperty(SCHEMA_VERSION, version);
    }

    public void setDeleted(boolean deleted) {
        entity.setProperty(DELETED, deleted);
    }

    public void setParentId(ResourceId parentId) {
        entity.setProperty(PARENT_PROPERTY, parentId.asString());
    }

    public static Key key(ResourceId collectionId, ResourceId resourceId) {
        Key parentKey = FormRootKey.key(collectionId);
        return KeyFactory.createKey(parentKey, KIND, resourceId.asString());
    }

    public void setFieldValues(FormClass formClass, Map<ResourceId, FieldValue> values) {
        for (Map.Entry<ResourceId, FieldValue> entry : values.entrySet()) {
            FormField field = formClass.getField(entry.getKey());
            FieldConverter converter = FieldConverters.forType(field.getType());
            if (entry.getValue() != null) {
                setFieldValue(field.getName(), converter.toHrdProperty(entry.getValue()));
            }         
        }
    }
    
    public void setFieldValue(String propertyName, Object value) {
        entity.setUnindexedProperty(propertyName, value);
    }

    public Entity raw() {
        return entity;
    }
    
    public static Query.FilterPredicate parentFilter(ResourceId parentId) {
        return new Query.FilterPredicate(PARENT_PROPERTY, Query.FilterOperator.EQUAL, parentId.asString());
    }

    public static Query.FilterPredicate deletedFilter(boolean deleted) {
        return new Query.FilterPredicate(DELETED, Query.FilterOperator.EQUAL, deleted);
    }

}
