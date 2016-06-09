package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.activityinfo.model.resource.ResourceId;

/**
 * Stores the current version of a Form Submission.
 * 
 * <p>Member of the Collection entity group.</p>
 */
public class FormSubmission implements TypedEntity {
    
    public static final String KIND = "Submission";

    /**
     * For sub form submissions, the parent form submission id. Indexed.
     */
    public static final String PARENT_PROPERTY = "@parent";
    
    private FormSubmissionKey key;
    private Entity entity;

    public FormSubmission(FormSubmissionKey key) {
        this.key = key;
        this.entity = new Entity(key.raw());
    }

    public FormSubmission(ResourceId submissionId) {
        this(new FormSubmissionKey(submissionId));
    }
    
    public FormSubmission(Entity entity) {
        this.key = new FormSubmissionKey(entity.getKey());
    }


    public FormSubmissionKey getKey() {
        return key;
    }

    public void setParentId(ResourceId parentId) {
        entity.setProperty(PARENT_PROPERTY, parentId.asString());
    }

    public static Key key(ResourceId collectionId, ResourceId resourceId) {
        Key parentKey = CollectionRootKey.key(collectionId);
        return KeyFactory.createKey(parentKey, KIND, resourceId.asString());
    }

    public void setProperty(String propertyName, Object value) {
        entity.setUnindexedProperty(propertyName, value);
    }

    public Entity raw() {
        return entity;
    }
}
