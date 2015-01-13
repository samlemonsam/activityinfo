package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.store.query.impl.views.BitSetColumnView;
import org.activityinfo.store.query.impl.views.BitSetWithMissingView;
import org.activityinfo.service.store.CursorObserver;

import java.util.BitSet;

public class BooleanColumnBuilder implements ColumnViewBuilder, CursorObserver<FieldValue> {

    private BitSet values = new BitSet();
    private BitSet missing = new BitSet();
    private int index = 0;

    private PendingSlot<ColumnView> result = new PendingSlot<>();

    @Override
    public void onNext(FieldValue value) {
        if(value instanceof BooleanFieldValue) {
            values.set(index, value == BooleanFieldValue.TRUE);
        } else {
            missing.set(index, true);
        }
        index++;
    }

    @Override
    public void onClosed() {
        int numRows = index;
        if(missing.isEmpty()) {
            result.set(new BitSetColumnView(numRows, values));
        } else {
            result.set(new BitSetWithMissingView(numRows, values, missing));
        }
    }

    @Override
    public ColumnView get() {
        return result.get();
    }

    @Override
    public void setFromCache(ColumnView view) {
        result.set(view);
    }
}
