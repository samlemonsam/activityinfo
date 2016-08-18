package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.resource.ResourceId;

import java.util.Date;


public class FormRecordSnapshotEntity implements TypedEntity {

    public static final String RECORD_PROPERTY = "record";
    public static final String PARENT_PROPERTY = "parentId";
    public static final String TIME_PROPERTY = "time";
    public static final String USER_PROPERTY = "user";

    private Entity entity;

    public FormRecordSnapshotEntity(long userId, FormRecordEntity record) {
        entity = new Entity(FormRecordSnapshotKey.KIND, record.getVersion(), record.raw().getKey());
        entity.setUnindexedProperty(RECORD_PROPERTY, record.raw());

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



    @Override
    public Entity raw() {
        return entity;
    }
}
