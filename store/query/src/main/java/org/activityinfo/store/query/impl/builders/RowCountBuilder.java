package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CursorObserver;

/**
 * Counts the number of rows in a set
 */
public class RowCountBuilder implements CursorObserver<ResourceId> {
    private int count = 0;
    
    @Override
    public void onNext(ResourceId value) {
        count++;
    }

    @Override
    public void done() {
    }


    public int getCount() {
        return count;
    }
}
