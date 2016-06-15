package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.store.hrd.FormConverter;

/**
 * Entity stored 
 */
public class FormSchemaEntity implements TypedEntity {

    public static final String OWNER_PROPERTY = "owner";

    public static final String SCHEMA_PROPERTY = "schema";
    public static final String KIND = "FormSchema";

    private FormSchemaKey key;
    private Entity entity;

    public FormSchemaEntity(Entity entity) {
        this.key = new FormSchemaKey(entity.getKey());
        this.entity = entity;
    }
    
    public FormSchemaEntity(FormClass formClass) {

        if(formClass.getOwnerId() == null) {
            throw new IllegalArgumentException("FormClass " + formClass.getId() + " has no @owner");
        }
        
        this.key = new FormSchemaKey(formClass.getId());
        this.entity = new Entity(key.raw());
        entity.setProperty(OWNER_PROPERTY, formClass.getOwnerId().asString());
        entity.setProperty(SCHEMA_PROPERTY, FormConverter.toEmbeddedEntity(formClass.asResource()));
    }
    
    public long getSchemaVersion() {
        return (Long)entity.getProperty("version");
    }
    
    public void setSchemaVersion(long version) {
        entity.setUnindexedProperty("version", version);
    }

    public FormClass readFormClass() {

        Object recordProperty = entity.getProperty(SCHEMA_PROPERTY);
        if(recordProperty == null) {
            throw new IllegalStateException(String.format("Entity %s is missing record property '%s'",
                    entity.getKey(), SCHEMA_PROPERTY));
        }
        if(!(recordProperty instanceof EmbeddedEntity)) {
            throw new IllegalArgumentException(String.format("Entity %s has record property '%s' of unexpected type '%s'.",
                    entity.getKey(), SCHEMA_PROPERTY, recordProperty.getClass().getName()));
        }

        Object ownerValue = entity.getProperty(OWNER_PROPERTY);
        if(!(ownerValue instanceof String)) {
            throw new IllegalStateException(String.format("Entity %s has invalid %s property: %s",
                    key, OWNER_PROPERTY, ownerValue));
        }
        
        Record formClassRecord = FormConverter.fromEmbeddedEntity(((EmbeddedEntity) recordProperty));

        Resource resource = Resources.createResource();
        resource.setId(key.getCollectionId());
        resource.setOwnerId(ResourceId.valueOf((String) ownerValue));
        resource.getProperties().putAll(formClassRecord.getProperties());

        return FormClass.fromResource(resource);
    }

    @Override
    public Entity raw() {
        return entity;
    }

    public void update(FormClass formClass) {
        entity.setProperty(SCHEMA_PROPERTY, FormConverter.toEmbeddedEntity(formClass.asResource()));
    }
}
