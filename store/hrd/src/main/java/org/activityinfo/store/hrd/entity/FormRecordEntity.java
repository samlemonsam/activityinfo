package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.FieldConverter;
import org.activityinfo.store.hrd.FieldConverters;

/**
 * Stores the current version of a Form Submission.
 * 
 * <p>Member of the Collection entity group.</p>
 */
public class FormRecordEntity implements TypedEntity {
    
    public static final String KIND = "FormRecord";

    /**
     * For sub form submissions, the parent form submission id. Indexed.
     */
    public static final String PARENT_PROPERTY = "@parent";
    
    private FormRecordKey key;
    private final Entity entity;

    public FormRecordEntity(FormRecordKey key) {
        this.key = key;
        this.entity = new Entity(key.raw());
    }

    public FormRecordEntity(ResourceId submissionId) {
        this(new FormRecordKey(submissionId));
    }
    
    public FormRecordEntity(Entity entity) {
        this.key = new FormRecordKey(entity.getKey());
        this.entity = entity;
    }
    
    public FormRecord toFormRecord(FormClass formClass) {
        FormRecord.Builder record = FormRecord.builder();
        record.setFormId(formClass.getId());
        record.setRecordId(key.getResourceId());

        if(formClass.getParentField().isPresent()) {
            record.setParentRecordId(getParentId());
        }
//
//        if(entity.getProperty("keyId") != null) {
//            formInstance.setKeyId(ResourceId.valueOf((String) entity.getProperty("keyId")));
//        }

        for (FormField formField : formClass.getFields()) {
            Object value = entity.getProperty(formField.getName());
            if(value != null) {
                FieldConverter<?> converter = FieldConverters.forType(formField.getType());
                record.setFieldValue(formField.getId(), converter.toFieldValue(value));
            }
        }
        return record.build();    
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
    
    public void setParentId(ResourceId parentId) {
        entity.setProperty(PARENT_PROPERTY, parentId.asString());
    }

    public static Key key(ResourceId collectionId, ResourceId resourceId) {
        Key parentKey = FormRootKey.key(collectionId);
        return KeyFactory.createKey(parentKey, KIND, resourceId.asString());
    }

    public void setProperty(String propertyName, Object value) {
        entity.setUnindexedProperty(propertyName, value);
    }

    public Entity raw() {
        return entity;
    }
    
    public static Query.FilterPredicate parentFilter(ResourceId parentId) {
        return new Query.FilterPredicate(PARENT_PROPERTY, Query.FilterOperator.EQUAL, parentId.asString());
    }
}
