package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.type.FieldValue;

import java.util.function.Function;

public class NumberBlock implements BlockManager {

    private Function<FieldValue, Number> reader;

    public NumberBlock(Function<FieldValue, Number> reader) {
        this.reader = reader;
    }

    @Override
    public int getBlockSize() {
        return 10_000;
    }

    @Override
    public Entity update(Entity blockEntity, int recordIndex, FieldValue fieldValue) {

        throw new UnsupportedOperationException("TODO");
    }


}
