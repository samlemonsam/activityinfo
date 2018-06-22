package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterator;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.FormColumnStorage;

public class NullBlock implements BlockManager {

    public static final NullBlock INSTANCE = new NullBlock();

    private NullBlock() {
    }

    @Override
    public int getBlockSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, FieldValue fieldValue) {
        return blockEntity;
    }

    @Override
    public ColumnView buildView(FormColumnStorage header, QueryResultIterator<Entity> blockIterator) {
        throw new UnsupportedOperationException("TODO");
    }

}
