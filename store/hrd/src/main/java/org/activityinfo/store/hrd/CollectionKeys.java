package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.activityinfo.model.resource.ResourceId;

/**
 * Defines the key structure for resource collections in the HRD
 */
public class CollectionKeys {

    public static final String COLLECTION_KIND = "Collection";
    public static final String CLASS_KIND = "FormClass";
    public static final String SUBMISSION_KIND = "Submission";

    /**
     * Root level key for a resource collection. 
     * 
     * <p>This forms the basis of the entity group, within which we can make transactions.</p>
     */
    public static Key collectionKey(ResourceId id) {
        return KeyFactory.createKey(COLLECTION_KIND, id.asString());
    }

    /**
     * Entity in which the FormClass is stored.
     */
    public static Key formClassKey(ResourceId resourceId) {
        return KeyFactory.createKey(collectionKey(resourceId), CLASS_KIND, 1);        
    }

    public static Key resourceKey(ResourceId collectionId, ResourceId resourceId) {
        Key parentKey = collectionKey(collectionId);
        return KeyFactory.createKey(parentKey, SUBMISSION_KIND, resourceId.asString());
    }
}
