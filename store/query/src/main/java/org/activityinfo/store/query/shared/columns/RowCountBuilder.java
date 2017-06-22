package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.spi.CursorObserver;

/**
 * Counts the number of rows in a set
 */
public class RowCountBuilder implements CursorObserver<ResourceId> {

    private final PendingSlot<Integer> resultSlot;

    private int count = 0;

    public RowCountBuilder(PendingSlot<Integer> resultSlot) {
        this.resultSlot = resultSlot;
    }

    @Override
    public void onNext(ResourceId value) {
        count++;
    }

    @Override
    public void done() {
        resultSlot.set(count);
    }

    public int getCount() {
        return count;
    }
}
