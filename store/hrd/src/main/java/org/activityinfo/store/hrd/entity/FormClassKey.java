package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Preconditions;
import org.activityinfo.model.resource.ResourceId;


public class FormClassKey implements TypedKey<FormClassEntity> {
    
    private Key key;
    
    public FormClassKey(ResourceId collectionId) {
        this.key = KeyFactory.createKey(CollectionRootKey.key(collectionId), FormClassEntity.CLASS_KIND, 1);

    }

    public FormClassKey(Key key) {
        Preconditions.checkArgument(key.getKind().equals(FormClassEntity.CLASS_KIND));
        Preconditions.checkArgument(key.getParent().getKind().equals(CollectionRootKey.KIND));
        this.key = key;
    }

    
    public ResourceId getCollectionId() {
        return ResourceId.valueOf(key.getParent().getName());
    }
    
    @Override
    public FormClassEntity typeEntity(Entity entity) {
        return new FormClassEntity(entity);
    }

    @Override
    public Key raw() {
        return key;
    }
}
