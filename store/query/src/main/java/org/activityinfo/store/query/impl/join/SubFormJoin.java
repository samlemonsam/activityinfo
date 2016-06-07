package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.Slot;

/**
 * Join between a master and sub form.
 */
public class SubFormJoin {

    private Slot<PrimaryKeyMap> masterPrimaryKey;
    private Slot<ColumnView> parentColumn;

    public SubFormJoin(Slot<PrimaryKeyMap> masterPrimaryKey, Slot<ColumnView> parentColumn) {
        this.masterPrimaryKey = masterPrimaryKey;
        this.parentColumn = parentColumn;
    }

    public Slot<PrimaryKeyMap> getMasterPrimaryKey() {
        return masterPrimaryKey;
    }

    public Slot<ColumnView> getParentColumn() {
        return parentColumn;
    }
}
