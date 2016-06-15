package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Preconditions;
import org.activityinfo.model.resource.ResourceId;


public class FormSchemaKey implements TypedKey<FormSchemaEntity> {
    
    private Key key;
    
    public FormSchemaKey(ResourceId collectionId) {
        this.key = KeyFactory.createKey(FormRootKey.key(collectionId), FormSchemaEntity.KIND, 1);

    }

    public FormSchemaKey(Key key) {
        Preconditions.checkArgument(key.getKind().equals(FormSchemaEntity.KIND));
        Preconditions.checkArgument(key.getParent().getKind().equals(FormRootKey.KIND));
        this.key = key;
    }

    
    public ResourceId getCollectionId() {
        return ResourceId.valueOf(key.getParent().getName());
    }
    
    @Override
    public FormSchemaEntity typeEntity(Entity entity) {
        return new FormSchemaEntity(entity);
    }

    @Override
    public Key raw() {
        return key;
    }
}
