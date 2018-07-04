package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.activityinfo.model.resource.ResourceId;

/**
 * Tracks deleted records with a bit mask.
 */
public class TombstoneBlock {

    public static final int BLOCK_SIZE = 1024 * 8 * 8;

    private static final String COLUMN_NAME = "__DELETED";
    private static final String BITSET_PROPERTY = "deleted";


    public BlockDescriptor getBlockDescriptor(ResourceId formId, int recordIndex) {
        int blockIndex = recordIndex / BLOCK_SIZE;
        int blockSize =  BLOCK_SIZE;
        return new BlockDescriptor(formId, COLUMN_NAME,
                blockIndex,
                blockIndex * blockSize,
                blockSize);
    }

    public void markDeleted(Entity blockEntity, int recordOffset) {
        BlobBitSet.update(blockEntity, BITSET_PROPERTY, recordOffset, true);
    }

    public static Key columnKey(ResourceId formId) {
        return BlockDescriptor.columnKey(formId, COLUMN_NAME);
    }

    public static byte[] getBitset(Entity entity) {
        return BlobBitSet.read(entity, BITSET_PROPERTY);
    }
}
