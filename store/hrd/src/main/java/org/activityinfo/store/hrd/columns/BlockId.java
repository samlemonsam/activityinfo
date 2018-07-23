package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Preconditions;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;

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

    public static Key columnKey(ResourceId formId, String fieldId) {
        Key formKey = FormEntity.key(formId).getRaw();
        return KeyFactory.createKey(formKey, COLUMN_KIND, fieldId);
    }

    public static Key columnKey(String formId, ResourceId fieldId) {
        return columnKey(ResourceId.valueOf(formId), fieldId.asString());
    }

    public static Key columnKey(ResourceId formId, ResourceId fieldId) {
        return columnKey(formId, fieldId.asString());
    }
}
