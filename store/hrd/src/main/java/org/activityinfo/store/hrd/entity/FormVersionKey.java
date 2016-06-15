package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.activityinfo.model.resource.ResourceId;


public class FormVersionKey implements TypedKey<FormVersionEntity> {
    
    public static final String KIND = "Version";
    
    private Key key;
    
    public FormVersionKey(FormRootKey rootKey) {
        this.key = KeyFactory.createKey(rootKey.raw(), KIND, 1);
    }

    public FormVersionKey(ResourceId collectionId) {
        this(new FormRootKey(collectionId));
    }

    @Override
    public FormVersionEntity typeEntity(Entity entity) {
        return new FormVersionEntity(entity);
    }

    @Override
    public Key raw() {
        return key;
    }
}
