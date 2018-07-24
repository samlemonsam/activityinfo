package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Preconditions;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;

import java.util.Objects;

/**
 * Identifies a single block
 */
public class BlockId {
    public static final String BLOCK_KIND = "Block";
    public static final String COLUMN_KIND = "Column";

    private ResourceId formId;
    private String columnId;
    private int blockIndex;

    public BlockId(ResourceId formId, String columnId, int blockIndex) {
        this.formId = formId;
        this.columnId = columnId;
        this.blockIndex = blockIndex;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public String getColumnId() {
        return columnId;
    }

    /**
     * @return zero-based index of the block
     */
    public int getBlockIndex() {
        return blockIndex;
    }

    public String memcacheKey(long version) {
        return getFormId() + "." + getColumnId() + "." + getBlockIndex() + "@" + version;
    }

    /**
     * Computes the zero-based offset of the record within this block from a one-based record number.
     */
    public int getOffset(int recordNumber, int blockLength) {
        Preconditions.checkArgument(recordNumber >= 1, "expected one-based index");

        int blockStart = blockIndex * blockLength;
        int zeroBasedRecordIndex = recordNumber - 1;
        int recordOffset = zeroBasedRecordIndex - blockStart;

        Preconditions.checkArgument(recordOffset >= 0 && recordOffset < blockLength,
                "record not present in block");

        return recordOffset;
    }

    public Key key() {
        return KeyFactory.createKey(columnKey(formId, columnId), BLOCK_KIND, blockIndex + 1);
    }

    public static Key columnKey(ResourceId formId, String columnId) {
        Key formKey = FormEntity.key(formId).getRaw();
        return KeyFactory.createKey(formKey, COLUMN_KIND, columnId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockId blockId = (BlockId) o;
        return blockIndex == blockId.blockIndex &&
                Objects.equals(formId, blockId.formId) &&
                Objects.equals(columnId, blockId.columnId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(formId, columnId, blockIndex);
    }
}
