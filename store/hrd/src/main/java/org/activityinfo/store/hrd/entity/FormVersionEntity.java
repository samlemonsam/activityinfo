package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.resource.ResourceId;

/**
 * Stores the current version of the collection
 */
public class FormVersionEntity implements TypedEntity {

    private final Entity entity;

    public FormVersionEntity(Entity entity) {
        this.entity = entity;
    }

    public FormVersionEntity(ResourceId collectionId) {
        this.entity = new Entity(new FormVersionKey(collectionId).raw());
    }

    public long getVersion() {
        return (Long)entity.getProperty("version");
    }
    
    public void setVersion(long version) {
        entity.setUnindexedProperty("version", version);
    }
    
    public long getSchemaVersion() {
        return (Long) entity.getProperty("schemaVersion");
    }
    
    public void setSchemaVersion(long version) {
        entity.setUnindexedProperty("schemaVersion", version);
    }

    @Override
    public Entity raw() {
        return entity;
    }
}
