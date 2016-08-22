package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.store.hrd.FormConverter;

/**
 * Entity stored 
 */
@Entity(name = "FormSchema")
public class FormSchemaEntity {

    private static final long ENTITY_ID = 1L;
    
    @Parent
    private Key<FormEntity> formKey;
    
    @Id
    private long id;
    
    @Index
    private String owner;
    
    @Unindex
    private long version;

    @Unindex
    private EmbeddedEntity schema;


    public FormSchemaEntity() {
    }

    public FormSchemaEntity(FormClass formClass) {

        if(formClass.getOwnerId() == null) {
            throw new IllegalArgumentException("FormClass " + formClass.getId() + " has no @owner");
        }
        
        this.formKey = FormEntity.key(formClass);
        this.id = ENTITY_ID;
        this.owner = formClass.getOwnerId().asString();
        this.schema = FormConverter.toEmbeddedEntity(formClass.asResource());
    }
    
    public static com.googlecode.objectify.Key<FormSchemaEntity> key(ResourceId formId) {
        return com.googlecode.objectify.Key.create(FormEntity.key(formId), FormSchemaEntity.class, ENTITY_ID);
    }

    public ResourceId getFormId() {
        return ResourceId.valueOf(formKey.getName());
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setSchema(FormClass formClass) {
        schema = FormConverter.toEmbeddedEntity(formClass.asResource());
    }

    public EmbeddedEntity getSchema() {
        return schema;
    }

    public FormClass readFormClass() {
        Preconditions.checkNotNull(owner, "owner");
        Preconditions.checkNotNull(owner, "schema");

        Record formClassRecord = FormConverter.fromEmbeddedEntity(schema);
        Resource resource = Resources.createResource();
        resource.setId(getFormId());
        resource.setOwnerId(ResourceId.valueOf(owner));
        resource.getProperties().putAll(formClassRecord.getProperties());

        return FormClass.fromResource(resource);
    }
}
