package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.type.FieldValue;

public class LocalDateBlock implements BlockManager {

    @Override
    public int getBlockSize() {
        return 20_000;
    }

    @Override
    public Entity update(Entity blockEntity, int recordIndex, FieldValue fieldValue) {
        throw new UnsupportedOperationException("TODO");
    }
}
