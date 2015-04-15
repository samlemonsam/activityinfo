package org.activityinfo.store.query.impl.join;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.service.store.CursorObserver;


public class ForeignKeyBuilder implements CursorObserver<FieldValue>, Slot<ForeignKeyMap> {

    private int rowIndex = 0;
    private Multimap<Integer, ResourceId> keys = HashMultimap.create();

    private PendingSlot<ForeignKeyMap> result = new PendingSlot<>();

    @Override
    public void onNext(FieldValue fieldValue) {
        if(fieldValue instanceof ReferenceValue) {
            ReferenceValue referenceValue = (ReferenceValue) fieldValue;
            for (ResourceId id : referenceValue.getResourceIds()) {
                keys.put(rowIndex, id);
            }
        }
        rowIndex++;
    }

    @Override
    public void done() {
        result.set(new ForeignKeyMap(rowIndex, keys));
    }

    public ForeignKeyMap get() {
        return result.get();
    }
}
