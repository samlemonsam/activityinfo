package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.type.FieldValue;

public interface BlockManager {

    int MAX_ENTITY_SIZE = 1_048_000;

    int getBlockSize();

    default int getBlockIndex(int recordIndex) {
        return Math.floorDiv(recordIndex - 1, getBlockSize());
    }

    Entity update(Entity blockEntity, int recordIndex, FieldValue fieldValue);
}

