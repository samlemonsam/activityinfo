package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Blob;
import org.activityinfo.store.hrd.columns.IntValueArray;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes a "column" of blocks. Each column of blocks stores one or more fields.
 */
public class ColumnDescriptor {
    private String columnId;
    private String blockType;
    private int recordCount;

    /**
     * The names of the fields assigned to this block
     */
    private Set<String> fields;

    /**
     * An {@link org.activityinfo.store.hrd.columns.IntValueArray} containing the current
     * version of each block that has been written.
     */
    private Blob versionMap;

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public void addField(String name) {
        if(this.fields == null) {
            fields = new HashSet<>();
        }
        fields.add(name);
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    /**
     * @return the number of records stored per block
     */
    public int getRecordCount() {
        return recordCount;
    }

    public long getBlockVersion(int blockIndex) {
        return IntValueArray.get(versionMap, blockIndex);
    }

    public void setBlockVersion(int blockIndex, long version) {
        if(version > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Version " + version + " exceeds " + Integer.MAX_VALUE);
        }
        this.versionMap = IntValueArray.update(versionMap, blockIndex, (int) version);
    }
}
