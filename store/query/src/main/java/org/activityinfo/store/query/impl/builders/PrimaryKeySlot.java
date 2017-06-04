package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.EmptySlots;
import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.store.query.impl.join.PrimaryKeyMap;


public class PrimaryKeySlot implements Slot<PrimaryKeyMap> {

    public static final PrimaryKeySlot EMPTY = new PrimaryKeySlot(EmptySlots.STRING);

    private Slot<ColumnView> idSlot;
    private PrimaryKeyMap map;

    public PrimaryKeySlot(Slot<ColumnView> idSlot) {
        this.idSlot = idSlot;
    }

    @Override
    public PrimaryKeyMap get() {
        if(map == null) {
            map = new PrimaryKeyMap(idSlot.get());
        }
        return map;
    }
}
