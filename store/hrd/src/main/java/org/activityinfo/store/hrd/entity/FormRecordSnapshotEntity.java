package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.RecordChangeType;

import java.util.Date;


public class FormRecordSnapshotEntity implements TypedEntity {

    public static final String RECORD_PROPERTY = "record";
    public static final String PARENT_PROPERTY = "parentId";
    public static final String TIME_PROPERTY = "time";
    public static final String USER_PROPERTY = "user";
    public static final String CHANGE_TYPE_PROPERTY = "type";


    private Entity entity;

    public FormRecordSnapshotEntity(long userId, RecordChangeType changeType, FormRecordEntity record) {
        entity = new Entity(FormRecordSnapshotKey.KIND, record.getVersion(), record.raw().getKey());
        entity.setUnindexedProperty(RECORD_PROPERTY, embed(record.raw()));
        entity.setUnindexedProperty(CHANGE_TYPE_PROPERTY, changeType.name());

        // Set the parent id as an index field so we can find changes
        // of all sub records
        ResourceId parentId = record.getParentId();
        if(parentId != null) {
            entity.setProperty(PARENT_PROPERTY, parentId.asString());
        }
        
        // Set the (indexed) time property to the current time
        entity.setProperty(TIME_PROPERTY, new Date());
        
        // Set the (indexed) for users so we can find all changes by a specific user
        entity.setProperty(USER_PROPERTY, userId);
    }

    public FormRecordSnapshotEntity(Entity entity) {
        this.entity = entity;
    }
    
    public Date getTime() {
        return (Date) entity.getProperty(TIME_PROPERTY);
    }

    public long getUserId() {
        return (Long)entity.getProperty(USER_PROPERTY);
    }
    
    public long getVersion() {
        return entity.getKey().getId();
    }
    
    public RecordChangeType getType() {
        return RecordChangeType.valueOf((String)entity.getProperty(CHANGE_TYPE_PROPERTY));
    }
    
    public void setType(RecordChangeType changeType) {
        entity.setProperty(CHANGE_TYPE_PROPERTY, changeType.name());
    }

    public FormRecordEntity getFormRecord() {
        EmbeddedEntity recordProperty = (EmbeddedEntity) entity.getProperty(RECORD_PROPERTY);
        Entity record = new Entity(entity.getKey().getParent());
        record.setPropertiesFrom(recordProperty);
        return new FormRecordEntity(record);
    }

    private static EmbeddedEntity embed(Entity entity) {
        EmbeddedEntity embeddedEntity = new EmbeddedEntity();
        embeddedEntity.setPropertiesFrom(entity);
        return embeddedEntity;
    }

    @Override
    public Entity raw() {
        return entity;
    }
}
