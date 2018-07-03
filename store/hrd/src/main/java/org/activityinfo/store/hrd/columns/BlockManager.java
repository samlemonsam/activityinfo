package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.FormColumnStorage;

import javax.annotation.Nullable;
import java.util.Iterator;

public interface BlockManager {

    int MAX_ENTITY_SIZE = 1_048_000;

    int getBlockSize();

    default BlockDescriptor getBlockDescriptor(ResourceId formId, String fieldName, int recordIndex) {
        int blockIndex = getBlockIndex(recordIndex);
        int blockSize =  getBlockSize();
        return new BlockDescriptor(formId, fieldName,
                blockIndex,
                blockIndex * blockSize,
                blockSize);

    }

    default int getBlockIndex(int recordIndex) {
        return Math.floorDiv(recordIndex - 1, getBlockSize());
    }

    /**
     * Update a block with a new field value
     * @param blockEntity the data store entity for the block
     * @param recordOffset the zero-based index of the record, relative to the start of the block
     * @param fieldValue the new field value
     * @return
     */
    Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue);

    ColumnView buildView(FormColumnStorage header, Iterator<Entity> blockIterator);
}

