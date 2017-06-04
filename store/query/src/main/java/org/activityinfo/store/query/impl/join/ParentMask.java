package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.store.query.impl.TableFilter;

import java.util.BitSet;


public class ParentMask implements Slot<TableFilter> {

    private final Slot<PrimaryKeyMap> parentKeySlot;
    private final Slot<ColumnView> parentIdSlot;

    private TableFilter result = null;

    public ParentMask(Slot<PrimaryKeyMap> parentKeySlot, Slot<ColumnView> parentIdSlot) {
        this.parentKeySlot = parentKeySlot;
        this.parentIdSlot = parentIdSlot;
    }

    @Override
    public TableFilter get() {
        if(result == null) {
            BitSet bitSet = new BitSet();
            PrimaryKeyMap parentKeyMap = parentKeySlot.get();
            ColumnView parentView = parentIdSlot.get();

            for (int i = 0; i < parentView.numRows(); i++) {
                String parentId = parentView.getString(i);
                bitSet.set(i, parentKeyMap.contains(parentId));
            }
            result = new TableFilter(bitSet);
        }
        return result;
    }
}
