package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.store.query.impl.views.ConstantColumnView;


public class ConstantColumnBuilder implements Slot<ColumnView> {

    private Slot<Integer> rowCount;
    private FieldValue value;

    public ConstantColumnBuilder(Slot<Integer> rowCount, FieldValue value) {
        this.rowCount = rowCount;
        this.value = value;
    }

    @Override
    public ColumnView get() {
        return ConstantColumnView.create(rowCount.get(), value);
    }
}
