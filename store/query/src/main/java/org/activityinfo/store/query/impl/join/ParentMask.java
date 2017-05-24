package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.store.query.impl.views.BitSetColumnView;

import java.util.BitSet;


public class ParentMask implements Slot<ColumnView> {

    private ReferenceJoin join;
    private ColumnView columnView = null;

    public ParentMask(ReferenceJoin join) {
        this.join = join;
    }


    @Override
    public ColumnView get() {
        if(columnView == null) {
            int[] mapping = join.mapping();
            BitSet bitSet = new BitSet();
            for (int i = 0; i < mapping.length; i++) {
                bitSet.set(i, (mapping[i] != -1));
            }
            columnView = new BitSetColumnView(mapping.length, bitSet);
        }
        return columnView;
    }
}
