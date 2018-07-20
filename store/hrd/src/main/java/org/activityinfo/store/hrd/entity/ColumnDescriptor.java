package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Blob;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes a "column" of blocks. Each column of blocks stores one or more fields.
 */
public class ColumnDescriptor {
    private String columnId;
    private String blockType;

    /**
     * The names of the fields assigned to this block
     */
    private Set<String> fields;

    /**
     * A {@link org.activityinfo.store.hrd.columns.BlobBitSet} indicating which
     * blocks are in use.
     */
    private Blob bitset;

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public Blob getBitset() {
        return bitset;
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
}
