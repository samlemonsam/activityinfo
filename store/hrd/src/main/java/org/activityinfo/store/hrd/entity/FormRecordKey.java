package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Preconditions;
import org.activityinfo.model.resource.ResourceId;

/**
 * Key for FormRecords
 */
public class FormRecordKey implements TypedKey<FormRecordEntity> {
    
    private Key key;
    
    public FormRecordKey(ResourceId submissionId) {
        ResourceId.checkSubmissionId(submissionId);

        String[] parts = submissionId.asString().split("-");
        Key parent = FormRootKey.key(ResourceId.valueOf(parts[0]));
        String name = submissionId.asString().substring(submissionId.asString().indexOf("-") + 1);
        this.key = KeyFactory.createKey(parent, FormRecordEntity.KIND, name);
    }

    public FormRecordKey(Key key) {
        Preconditions.checkArgument(key.getParent().getKind().equals(FormRootKey.KIND));
        Preconditions.checkArgument(key.getKind().equals(FormRecordEntity.KIND));
        
        this.key = key;
    }

    public ResourceId getCollectionId() {
        return ResourceId.valueOf(key.getParent().getName());
    }

    public ResourceId getResourceId() {
        return ResourceId.valueOf(getCollectionId() + "-" + key.getName());
    }

    @Override
    public Key raw() {
        return key;
    }

    @Override
    public FormRecordEntity typeEntity(Entity entity) {
        return new FormRecordEntity(entity);
    }

}
