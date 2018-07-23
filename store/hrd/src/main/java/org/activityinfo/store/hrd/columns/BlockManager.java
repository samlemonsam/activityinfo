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

    /**
     * @return the number of records stored in this block
     */
    int getRecordCount();

    /**
     * @return the maximum number of fields that can be mapped to this column of blocks
     */
    int getMaxFieldSize();

    String getBlockType();

    default BlockId getBlockId(ResourceId formId, String columnId, int recordIndex) {
        return new BlockId(formId, columnId, getBlockIndex(recordIndex));

    }

    default int getBlockIndex(int recordIndex) {
        return Math.floorDiv(recordIndex - 1, getRecordCount());
    }

    /**
     * Update a block with a new field value
     * @param blockEntity the data store entity for the block
     * @param recordOffset the zero-based index of the record, relative to the start of the block
     * @param fieldValue the new field value
     * @return
     */
    Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue);

    ColumnView buildView(FormEntity header, TombstoneIndex deleted, Iterator<Entity> blockIterator, String component);

    default ColumnView buildView(FormEntity header, TombstoneIndex tombstones, Iterator<Entity> blockIterator) {
        return buildView(header, tombstones, blockIterator, null);
    }


    /**
     * @return true if this field can be assigned to the given column block.
     */
    default boolean canBeAssignedTo(ColumnDescriptor descriptor) {
        return descriptor.getBlockType().equals(getBlockType()) &&
                descriptor.getFields().size() < getMaxFieldSize();
    }
}

