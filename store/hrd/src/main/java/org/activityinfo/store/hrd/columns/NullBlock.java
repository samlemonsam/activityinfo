package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.FormEntity;

import java.util.Iterator;

public class NullBlock implements BlockManager {

    public static final NullBlock INSTANCE = new NullBlock();

    private NullBlock() {
    }

    @Override
    public int getRecordCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxFieldSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String getBlockType() {
        return "null";
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, FieldValue fieldValue) {
        return blockEntity;
    }

    @Override
    public ColumnView buildView(FormEntity header, TombstoneIndex deleted, Iterator<Entity> blockIterator, String component) {
        throw new UnsupportedOperationException("TODO");
    }

}
