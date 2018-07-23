package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.resource.ResourceId;

/**
 * Tracks deleted records with a bit mask.
 */
public class TombstoneBlock {

    public static final int BLOCK_SIZE = 1024 * 8 * 8;

    public static final String COLUMN_NAME = "__DELETED";
    private static final String BITSET_PROPERTY = "deleted";

    private TombstoneBlock() {}

    public static BlockId getBlockDescriptor(ResourceId formId, int recordIndex) {
        int blockIndex = recordIndex / BLOCK_SIZE;
        return new BlockId(formId, COLUMN_NAME, blockIndex);
    }

    public static void markDeleted(Entity blockEntity, int recordOffset) {
        BlobBitSet.update(blockEntity, BITSET_PROPERTY, recordOffset, true);
    }

    public static byte[] getBitset(Entity entity) {
        return BlobBitSet.read(entity, BITSET_PROPERTY);
    }
}
