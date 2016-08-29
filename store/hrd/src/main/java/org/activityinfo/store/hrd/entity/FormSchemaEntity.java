package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.FormConverter;

/**
 * Entity storing a Form's Schema.
 */
@Entity(name = "FormSchema")
public class FormSchemaEntity {

    private static final long ENTITY_ID = 1L;

    /**
     * The {@link FormEntity} key, the root key of the Form Entity Group.
     */
    @Parent
    private Key<FormEntity> formKey;

    /**
     * Always {@code 1L} as there is one {@code FormSchema} per Form Entity Group.
     */
    @Id
    private long id;

    /**
     * The id of the owner of this form. Currently will be either the id of a database
     * or an activity, in the case of subforms.
     */
    @Index
    private String owner;

    /**
     * The current version of the schema for this Form.
     */
    @Unindex
    private long schemaVersion;

    /**
     * The {@link FormClass} serialized as an {@link EmbeddedEntity} via {@link FormClass#toJsonObject()} ()}
     */
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
        this.schema = FormConverter.toEmbeddedEntity(formClass.toJsonObject());
    }
    
    public static com.googlecode.objectify.Key<FormSchemaEntity> key(ResourceId formId) {
        return com.googlecode.objectify.Key.create(FormEntity.key(formId), FormSchemaEntity.class, ENTITY_ID);
    }

    public ResourceId getFormId() {
        return ResourceId.valueOf(formKey.getName());
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public void setSchema(FormClass formClass) {
        schema = FormConverter.toEmbeddedEntity(formClass.toJsonObject());
    }

    public EmbeddedEntity getSchema() {
        return schema;
    }

    public FormClass readFormClass() {
        Preconditions.checkNotNull(owner, "owner");
        Preconditions.checkNotNull(owner, "schema");

        return FormClass.fromJson(FormConverter.fromEmbeddedEntity(schema));
    }
}
