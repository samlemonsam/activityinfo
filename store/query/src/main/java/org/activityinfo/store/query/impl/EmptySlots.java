package org.activityinfo.store.query.impl;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EmptyColumnView;
import org.activityinfo.store.query.impl.join.ForeignKey;
import org.activityinfo.store.query.impl.join.ForeignKey32;

public class EmptySlots {
    private EmptySlots() {}

    public static final Slot<ColumnView> STRING =
            new PendingSlot<ColumnView>(new EmptyColumnView(ColumnType.STRING, 0));

    public static final Slot<Integer> ZERO_ROW_COUNT = new PendingSlot<>(0);

    public static final Slot<ForeignKey> EMPTY_FK = new PendingSlot<ForeignKey>(ForeignKey32.zeroRows());
}
