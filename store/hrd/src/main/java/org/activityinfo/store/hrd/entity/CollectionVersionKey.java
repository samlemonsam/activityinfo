package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.activityinfo.model.resource.ResourceId;


public class CollectionVersionKey implements TypedKey<CollectionVersionEntity> {
    
    public static final String KIND = "Version";
    
    private Key key;
    
    public CollectionVersionKey(CollectionRootKey rootKey) {
        this.key = KeyFactory.createKey(rootKey.raw(), KIND, 1);
    }

    public CollectionVersionKey(ResourceId collectionId) {
        this(new CollectionRootKey(collectionId));
    }

    @Override
    public CollectionVersionEntity typeEntity(Entity entity) {
        return new CollectionVersionEntity(entity);
    }

    @Override
    public Key raw() {
        return key;
    }
}
