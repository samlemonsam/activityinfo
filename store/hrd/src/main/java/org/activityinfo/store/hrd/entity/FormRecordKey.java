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
        Preconditions.checkArgument(submissionId.getDomain() == ResourceId.GENERATED_ID_DOMAIN, malformedId(submissionId));

        String[] parts = submissionId.asString().split("-");
        Preconditions.checkArgument(parts.length == 2, malformedId(submissionId));
        
        Key parent = FormRootKey.key(ResourceId.valueOf(parts[0]));
        this.key = KeyFactory.createKey(parent, FormRecordEntity.KIND, parts[1]);
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
    
    private static String malformedId(ResourceId id) {
        return String.format("Invalid id: '%s'. Expected format: c{collectionId}-{submissionId}", id.asString());
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
