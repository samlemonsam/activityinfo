package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.shared.Slot;
import org.activityinfo.store.query.server.join.FastPrimaryKeyMap;
import org.activityinfo.store.query.shared.join.PrimaryKeyMap;


public class PrimaryKeySlot implements Slot<PrimaryKeyMap> {

    private Slot<ColumnView> idSlot;
    private PrimaryKeyMap map;

    public PrimaryKeySlot(Slot<ColumnView> idSlot) {
        this.idSlot = idSlot;
    }

    @Override
    public PrimaryKeyMap get() {
        if(map == null) {
            map = new FastPrimaryKeyMap(idSlot.get());
        }
        return map;
    }
}
