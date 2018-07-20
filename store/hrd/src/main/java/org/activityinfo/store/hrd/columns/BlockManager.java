package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.ColumnDescriptor;
import org.activityinfo.store.hrd.entity.FormEntity;

import javax.annotation.Nullable;
import java.util.Iterator;

public interface BlockManager {

    int MAX_ENTITY_SIZE = 1_048_000;

    int getBlockRowSize();

    /**
     * @return the maximum number of fields that can be mapped to this column of blocks
     */
    int getMaxFieldSize();

    String getBlockType();

    default BlockId getBlockDescriptor(ResourceId formId, String columnId, int recordIndex) {
        int blockIndex = getBlockIndex(recordIndex);
        int blockSize =  getBlockRowSize();
        return new BlockId(formId, columnId,
                blockIndex,
                blockIndex * blockSize,
                blockSize);

    }

    default int getBlockIndex(int recordIndex) {
        return Math.floorDiv(recordIndex - 1, getBlockRowSize());
    }

    /**
     * Update a block with a new field value
     * @param blockEntity the data store entity for the block
     * @param recordOffset the zero-based index of the record, relative to the start of the block
     * @param fieldValue the new field value
     * @return
     */
    Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue);

    ColumnView buildView(FormEntity header, TombstoneIndex deleted, Iterator<Entity> blockIterator);

    /**
     * @return true if this field can be assigned to the given column block.
     */
    default boolean canBeAssignedTo(ColumnDescriptor descriptor) {
        return descriptor.getBlockType().equals(getBlockType()) &&
                descriptor.getFields().size() < getMaxFieldSize();
    }
}

