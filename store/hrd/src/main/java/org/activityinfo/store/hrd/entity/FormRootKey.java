package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Preconditions;
import org.activityinfo.model.resource.ResourceId;

/**
 * Entity group that includes all entities related to a single collection. Ensures that
 * we can atomically update a collection.
 */
public class FormRootKey {
    
    public static final String KIND = "Collection";

    private Key key;
    
    public FormRootKey(ResourceId collectionId) {
        this.key = key(collectionId);
    }

    public FormRootKey(Key key) {
        Preconditions.checkArgument(key.getKind().equals(KIND));
        Preconditions.checkArgument(key.getParent() == null);
        this.key = key;
    }

    public ResourceId getCollectionId() {
        return ResourceId.valueOf(key.getName());
    }
    
    public FormSchemaKey classKey() {
        return new FormSchemaKey(getCollectionId());
    }
    
    public Key raw() {
        return key;
    }
    
    /**
     * Root level key for a resource collection. 
     * 
     * <p>This forms the basis of the entity group, within which we can make transactions.</p>
     */
    public static Key key(ResourceId id) {
        return KeyFactory.createKey(KIND, id.asString());
    }
}
