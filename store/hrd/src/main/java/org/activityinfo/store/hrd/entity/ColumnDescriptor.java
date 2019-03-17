package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Blob;
import org.activityinfo.store.hrd.columns.IntValueArray;
import org.activityinfo.store.hrd.columns.LongValueArray;

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
    @Deprecated
    private Blob versionMap;

    /**
     * An {@link org.activityinfo.store.hrd.columns.LongValueArray} containing the current
     * version of each block that has been written.
     */
    private Blob versionMap64;

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

    @SuppressWarnings("deprecation")
    public long getBlockVersion(int blockIndex) {
        if(versionMap64 != null) {
            return LongValueArray.get(versionMap64, blockIndex);
        }
        return IntValueArray.get(versionMap, blockIndex);
    }

    public void setBlockVersion(int blockIndex, long version) {
        // Migrate array of 32-bit ints to 64-bit integers
        // preserve the 32-bit version until all readers and writers have migrated to this change.
        if(versionMap != null && versionMap64 == null) {
            this.versionMap64 = LongValueArray.fromInt32(versionMap);
        }
        this.versionMap = IntValueArray.update(versionMap, blockIndex, wrapAroundCast(version));
        this.versionMap64 = LongValueArray.update(versionMap64, blockIndex, version);
    }

    static int wrapAroundCast(long version) {
        return (int) version;
    }
}
