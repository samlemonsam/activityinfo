package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.store.query.impl.views.BitSetColumnView;
import org.activityinfo.store.query.impl.views.BitSetWithMissingView;

import java.util.BitSet;

public class BooleanColumnBuilder implements CursorObserver<FieldValue> {

    private final PendingSlot<ColumnView> result;

    private BitSet values = new BitSet();
    private BitSet missing = new BitSet();
    private int index = 0;

    public BooleanColumnBuilder(PendingSlot<ColumnView> result) {
        this.result = result;
    }


    @Override
    public void onNext(FieldValue value) {
        if (value instanceof BooleanFieldValue) {
            values.set(index, value == BooleanFieldValue.TRUE);
        } else {
            missing.set(index, true);
        }
        index++;
    }

    @Override
    public void done() {
        int numRows = index;
        if (missing.isEmpty()) {
            result.set(new BitSetColumnView(numRows, values));
        } else {
            result.set(new BitSetWithMissingView(numRows, values, missing));
        }
    }
}
