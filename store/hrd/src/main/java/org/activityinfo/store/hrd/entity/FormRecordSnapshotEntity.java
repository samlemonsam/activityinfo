package org.activityinfo.store.hrd.entity;

import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import com.googlecode.objectify.cmd.LoadType;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.RecordChangeType;

import java.util.Date;
import java.util.logging.Logger;


@Entity(name = "FormRecordSnapshot")
public class FormRecordSnapshotEntity {
    
    @Parent
    private Key<FormRecordEntity> recordKey;
    
    @Id
    private long id;
    
    @Index
    private String parentRecordId;
    
    @Index
    private Date time;
    
    @Index
    private long userId;

    @Index
    private long version;
    
    @Unindex
    private RecordChangeType type;
    
    @Unindex
    private FormRecordEntity record;

    public FormRecordSnapshotEntity() {
    }

    public FormRecordSnapshotEntity(long userId, RecordChangeType changeType, FormRecordEntity record) {
        Preconditions.checkArgument(userId != 0);
        
        this.recordKey = Key.create(record);
        this.id = record.getVersion();
        this.version = record.getVersion();
        this.parentRecordId = record.getParentRecordId();
        this.type = changeType;
        this.userId = userId;
        this.time = new Date();
        this.record = record;
    }
    
    public ResourceId getRecordId() {
        return record.getRecordId();
    }

    public Key<FormRecordEntity> getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(Key<FormRecordEntity> recordKey) {
        this.recordKey = recordKey;
    }

    public long getVersion() {
        return id;
    }

    public void setVersion(long version) {
        this.id = version;
    }

    public String getParentRecordId() {
        return parentRecordId;
    }

    public void setParentRecordId(String parentRecordId) {
        this.parentRecordId = parentRecordId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public RecordChangeType getType() {
        return type;
    }

    public void setType(RecordChangeType type) {
        this.type = type;
    }

    public FormRecordEntity getRecord() {
        return record;
    }

    public void setRecord(FormRecordEntity record) {
        this.record = record;
    }

    public static void reindexSnapshots() {
        LoadType<FormRecordSnapshotEntity> query = ObjectifyService
                .ofy()
                .load()
                .type(FormRecordSnapshotEntity.class);

        Logger logger = Logger.getLogger(FormRecordSnapshotEntity.class.getName());

        for (FormRecordSnapshotEntity entity : query.iterable()) {
            logger.info(entity.getRecordKey() + " = " + entity.version);
            if(entity.version == 0) {
                entity.version = entity.id;
                ObjectifyService.ofy().save().entity(entity).now();
            }
        }
    }
}
