package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.views.ConstantColumnView;
import org.activityinfo.store.query.impl.Slot;


public class ConstantColumnBuilder implements Slot<ColumnView> {

    private Slot<Integer> rowCount;
    private Object value;

    public ConstantColumnBuilder(Slot<Integer> rowCount, Object value) {
        this.rowCount = rowCount;
        this.value = value;
    }

    @Override
    public ColumnView get() {
        return ConstantColumnView.create(rowCount.get(), value);
    }
}
