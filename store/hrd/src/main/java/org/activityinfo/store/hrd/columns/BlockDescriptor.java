package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Preconditions;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;

public class BlockDescriptor {
    public static final String BLOCK_KIND = "Block";
    public static final String COLUMN_KIND = "Column";
    private ResourceId formId;
    private String fieldId;
    private int blockIndex;
    private int blockStart;
    private int blockLength;

    public BlockDescriptor(ResourceId formId, String fieldId, int blockIndex, int blockStart, int blockLength) {
        this.formId = formId;
        this.fieldId = fieldId;
        this.blockIndex = blockIndex;
        this.blockStart = blockStart;
        this.blockLength = blockLength;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public String getFieldId() {
        return fieldId;
    }

    /**
     * @return zero-based index of the block
     */
    public int getBlockIndex() {
        return blockIndex;
    }


    /**
     * @return zero-based record index of the first record contained in this block
     */
    public int getBlockStart() {
        return blockStart;
    }

    /**
     * @return number of records contained in this block
     */
    public int getBlockLength() {
        return blockLength;
    }

    /**
     * Computes the zero-based offset of the record within this block from a one-based record index.
     */
    public int getOffset(int recordIndex) {
        Preconditions.checkArgument(recordIndex >= 1, "expected one-based index");

        int zeroBasedRecordIndex = recordIndex - 1;
        int recordOffset = zeroBasedRecordIndex - blockStart;

        Preconditions.checkArgument(recordOffset >= 0 && recordOffset < blockLength,
                "record not present in block");

        return recordOffset;
    }

    public Key key() {
        return KeyFactory.createKey(columnKey(formId, fieldId), BLOCK_KIND, blockIndex + 1);
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
