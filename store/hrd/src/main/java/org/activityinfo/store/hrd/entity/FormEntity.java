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

import com.google.appengine.api.datastore.Blob;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.columns.IntValueArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Parent entity of all entities related to a single form.
 * 
 * <p>The {@code Form} entity forms the root of the Form entity group, allowing us to offer
 * Serializable Consistency with respect to the records within a single Form. </p>
 * 
 */
@Entity(name = "Form")
public class FormEntity {

    @Id
    private String id;

    /**
     * The current version of this Form. The {@code version} is incremented whenever the {@link FormSchemaEntity} is
     * changed for this Form Entity Group, or if any changes are made to the {@link FormRecordEntity}s that belong
     * to this Form Group.
     */
    @Unindex
    private long version;

    /**
     * The current version of this Form's {@link FormSchemaEntity}. The {@code schemaVersion} will always
     * be less than or equal to the Form's overall {@code version}.
     */
    @Unindex
    private long schemaVersion;


    @Unindex
    private boolean columnStorageActive;

    /**
     * The total number of records created (since we started counting)
     */
    @Unindex
    private int recordCount;

    /**
     * The number of records that have been deleted (since we started counting)
     */
    @Unindex
    private int deletedCount;

    private Map<String, FieldDescriptor> fields;

    private Map<String, ColumnDescriptor> blockColumns;


    /**
     * Tracks the version of each tombstone block
     */
    private Blob tombstoneVersionMap;

    /**
     * The version number of last record od block version. Only the (currently)
     * last record block will change, the rest are essentially immutable.
     */
    private long tailIdBlockVersion;


    public FormEntity() {
    }

    public String getId() {
        return id;
    }


    public ResourceId getResourceId() {
        return ResourceId.valueOf(id);
    }

    public void setId(ResourceId id) {
        this.id = id.asString();
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public static Key<FormEntity> key(ResourceId formId) {
        return Key.create(FormEntity.class, formId.asString());
    }

    public static Key<FormEntity> key(FormClass formClass) {
        return key(formClass.getId());
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public boolean isColumnStorageActive() {
        return columnStorageActive;
    }

    public void setColumnStorageActive(boolean columnStorageActive) {
        this.columnStorageActive = columnStorageActive;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    public FieldDescriptor getFieldDescriptor(String fieldName) {
        if(fields == null) {
            fields = new HashMap<>();
        }
        FieldDescriptor descriptor = fields.get(fieldName);
        if(descriptor == null) {
            descriptor = new FieldDescriptor();
            descriptor.setVersion(version);

            fields.put(fieldName, descriptor);
        }

        return descriptor;
    }

    public Map<String, ColumnDescriptor> getBlockColumns() {
        if(blockColumns == null) {
            blockColumns = new HashMap<>();
        }
        return blockColumns;
    }

    public ColumnDescriptor getFieldBlock(String columnId) {
        ColumnDescriptor descriptor = this.blockColumns.get(columnId);
        if(descriptor == null) {
            throw new IllegalArgumentException("No such block: " + columnId);
        }
        return descriptor;
    }

    public void addFieldBlock(ColumnDescriptor column) {
        if(this.blockColumns == null) {
            this.blockColumns = new HashMap<>();
        }
        this.blockColumns.put(column.getColumnId(), column);
    }


    public long getTombstoneBlockVersion(int blockIndex) {
        return IntValueArray.get(tombstoneVersionMap, blockIndex);
    }

    public void setTombstoneBlockVersion(int blockIndex, long version) {
        if(version > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Version " + version + " exceeds " + Integer.MAX_VALUE);
        }
        this.tombstoneVersionMap = IntValueArray.update(tombstoneVersionMap, blockIndex, (int) version);
    }

    public long getTailIdBlockVersion() {
        return tailIdBlockVersion;
    }

    public void setTailIdBlockVersion(long tailIdBlockVersion) {
        this.tailIdBlockVersion = tailIdBlockVersion;
    }
}


