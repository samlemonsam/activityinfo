package org.activityinfo.store.hrd.entity;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.columns.RecordNumbering;

@Entity(name = "FormColumns")
public class FormColumnStorage {

    /**
     * The {@link FormEntity} key, the root key of the Form Entity Group.
     */
    @Parent
    private Key<FormEntity> formKey;

    @Id
    private String scheme;

    @Unindex
    private int recordCount;

    @Unindex
    private int deletedCount;

    @Index
    private long version;

    public FormColumnStorage() {
    }

    public FormColumnStorage(ResourceId formId, RecordNumbering scheme) {
        this.formKey = FormEntity.key(formId);
        this.scheme = scheme.name();
    }

    public Key<FormEntity> getFormKey() {
        return formKey;
    }

    public ResourceId getFormId() {
        return ResourceId.valueOf(formKey.getName());
    }

    public RecordNumbering getScheme() {
        return RecordNumbering.valueOf(scheme);
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public static Key<FormColumnStorage> key(FormEntity rootEntity) {
        return key(rootEntity, rootEntity.getActiveColumnStorage());
    }

    public static Key<FormColumnStorage> key(FormEntity rootEntity, RecordNumbering scheme) {
        return Key.create(Key.create(rootEntity), FormColumnStorage.class, scheme.name());
    }


}
