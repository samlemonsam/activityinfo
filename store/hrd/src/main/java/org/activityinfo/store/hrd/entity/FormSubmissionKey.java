package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Preconditions;
import org.activityinfo.model.resource.ResourceId;

/**
 * Key for FormSubmissionEntities
 */
public class FormSubmissionKey implements TypedKey<FormSubmission> {
    
    private Key key;
    
    public FormSubmissionKey(ResourceId submissionId) {
        Preconditions.checkArgument(submissionId.getDomain() == ResourceId.GENERATED_ID_DOMAIN, malformedId(submissionId));

        String[] parts = submissionId.asString().split("-");
        Preconditions.checkArgument(parts.length == 2, malformedId(submissionId));
        
        Key parent = CollectionRootKey.key(ResourceId.valueOf(parts[0]));
        this.key = KeyFactory.createKey(parent, FormSubmission.KIND, parts[0]);
    }

    public FormSubmissionKey(Key key) {
        Preconditions.checkArgument(key.getParent().getKind().equals(CollectionRootKey.KIND));
        Preconditions.checkArgument(key.getKind().equals(FormSubmission.KIND));
        
        this.key = key;
    }

    public ResourceId getCollectionId() {
        return ResourceId.valueOf(key.getParent().getName());
    }
    
    private static String malformedId(ResourceId id) {
        return String.format("Invalid id: '%s'. Expected format: c{collectionId}-{submissionId}", id.asString());
    }

    @Override
    public Key raw() {
        return key;
    }

    @Override
    public FormSubmission typeEntity(Entity entity) {
        return new FormSubmission(entity);
    }
}
