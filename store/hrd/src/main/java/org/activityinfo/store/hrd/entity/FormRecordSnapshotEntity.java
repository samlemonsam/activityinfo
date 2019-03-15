/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.store.hrd.entity;

import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import com.googlecode.objectify.condition.IfFalse;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.RecordChangeType;

import java.util.Date;


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
    @IgnoreLoad
    private RecordChangeType type;
    
    @Unindex
    private FormRecordEntity record;

    @Unindex
    @IgnoreSave(IfFalse.class)
    private boolean migrated;


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

    public boolean isMigrated() {
        return migrated;
    }

    public void setMigrated(boolean migrated) {
        this.migrated = migrated;
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


    void migrateChangeType(@AlsoLoad("type") String type) {
        if ("CREATED".equals(type))
            this.type = RecordChangeType.ADDED;
        else
            this.type = RecordChangeType.valueOf(type);
    }
}
