package org.activityinfo.store.hrd.entity;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

/**
 * Parent entity of all entities related to a single form.
 * 
 * <p>The {@code Form} entity forms the root of the Form entity group, allowing us to offer
 * Serializable Consistency with respect to the records within a single Form. </p>
 * 
 */
@Entity(name = "Form")
public class FormEntity {
    
    @Id
    private String id;

    /**
     * The current version of this Form. The {@code version} is incremented whenever the {@link FormSchemaEntity} is
     * changed for this Form Entity Group, or if any changes are made to the {@link FormRecordEntity}s that belong
     * to this Form Group.
     */
    @Unindex
    private long version;

    /**
     * The current version of this Form's {@link FormSchemaEntity}. The {@code schemaVersion} will always
     * be less than or equal to the Form's overall {@code version}.
     */
    @Unindex
    private long schemaVersion;

    public FormEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(ResourceId id) {
        this.id = id.asString();
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public static Key<FormEntity> key(ResourceId formId) {
        return Key.create(FormEntity.class, formId.asString());
    }

    public static Key<FormEntity> key(FormClass formClass) {
        return key(formClass.getId());
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

}
