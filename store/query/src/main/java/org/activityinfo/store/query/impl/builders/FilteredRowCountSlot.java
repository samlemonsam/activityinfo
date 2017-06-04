package org.activityinfo.store.query.impl.builders;

import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.store.query.impl.TableFilter;

public class FilteredRowCountSlot implements Slot<Integer> {
    private final Slot<Integer> countSlot;
    private final Slot<TableFilter> filterSlot;

    private Integer value;

    public FilteredRowCountSlot(Slot<Integer> countSlot, Slot<TableFilter> filterSlot) {
        this.countSlot = countSlot;
        this.filterSlot = filterSlot;
    }

    @Override
    public Integer get() {
        if(value == null) {
            TableFilter filter = filterSlot.get();
            if(filter.isAllSelected()) {
                value = countSlot.get();
            } else {
                value = filter.getBitSet().cardinality();
            }
        }
        return value;
    }
}
