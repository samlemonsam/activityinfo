package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.shared.Slot;
import org.activityinfo.store.query.shared.TableFilter;


public class FilteredSlot implements Slot<ColumnView> {
    private Slot<TableFilter> filterSlot;
    private Slot<ColumnView> columnViewSlot;

    private ColumnView filtered = null;

    public FilteredSlot(Slot<TableFilter> filterSlot, Slot<ColumnView> columnViewSlot) {
        this.filterSlot = filterSlot;
        this.columnViewSlot = columnViewSlot;
    }


    @Override
    public ColumnView get() {
        if(filtered == null) {
            filtered = filterSlot.get().apply(columnViewSlot.get());
        }
        return filtered;
    }
}
