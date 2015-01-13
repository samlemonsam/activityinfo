package org.activityinfo.store.query.impl.join;

import com.google.common.collect.Maps;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.service.store.CursorObserver;

import java.util.Map;

/**
 * Builds a mapping from {@code ResourceId} to row index
 */
public class PrimaryKeyMapBuilder implements CursorObserver<ResourceId>, Slot<PrimaryKeyMap> {

    private final PendingSlot<PrimaryKeyMap> result = new PendingSlot<>();
    private final Map<ResourceId, Integer> map = Maps.newHashMap();

    private int rowIndex = 0;

    @Override
    public void onNext(ResourceId id) {
        map.put(id, rowIndex++);
    }

    @Override
    public void onClosed() {
        result.set(new PrimaryKeyMap(map));
    }

    public PrimaryKeyMap get() {
        return result.get();
    }
}
