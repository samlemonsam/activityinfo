package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.ConstantColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.store.spi.CursorObserver;


public class UnsupportedColumnTypeBuilder implements CursorObserver<FieldValue> {

    private int rows = 0;

    private final PendingSlot<ColumnView> result;

    public UnsupportedColumnTypeBuilder(PendingSlot<ColumnView> result) {
        this.result = result;
    }


    @Override
    public void onNext(FieldValue value) {
        rows++;
    }

    @Override
    public void done() {
        result.set(new ConstantColumnView(rows, (String)null));
    }
}
