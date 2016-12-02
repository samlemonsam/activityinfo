package org.activityinfo.store.hrd.entity;

import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.RecordChangeType;

import java.util.Date;


@Entity(name = "FormRecordSnapshot")
public class FormRecordSnapshotEntity {
    
    @Parent
    private Key<FormRecordEntity> recordKey;
    
    @Id
    private long version;
    
    @Index
    private String parentRecordId;
    
    @Index
    private Date time;
    
    @Index
    private long userId;
    
    @Unindex
    private RecordChangeType type;
    
    @Unindex
    private FormRecordEntity record;

    public FormRecordSnapshotEntity() {
    }

    public FormRecordSnapshotEntity(long userId, RecordChangeType changeType, FormRecordEntity record) {
        Preconditions.checkArgument(userId != 0);
        
        this.recordKey = Key.create(record);
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
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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
}
