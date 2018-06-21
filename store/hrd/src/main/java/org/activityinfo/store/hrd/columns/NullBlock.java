package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.type.FieldValue;

public class NullBlock implements BlockManager {

    public static final NullBlock INSTANCE = new NullBlock();

    private NullBlock() {
    }

    @Override
    public int getBlockSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Entity update(Entity blockEntity, int recordIndex, FieldValue fieldValue) {
        return blockEntity;
    }

}
