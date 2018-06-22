package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterator;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.FormColumnStorage;

import javax.annotation.Nullable;

public class MultiEnumBlock implements BlockManager {
    @Override
    public int getBlockSize() {
        return 10_000;
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ColumnView buildView(FormColumnStorage header, QueryResultIterator<Entity> blockIterator) {
        throw new UnsupportedOperationException("TODO");
    }
}
