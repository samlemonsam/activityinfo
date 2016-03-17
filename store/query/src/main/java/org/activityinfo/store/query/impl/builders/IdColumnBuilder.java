package org.activityinfo.store.query.impl.builders;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.store.query.impl.views.StringArrayColumnView;

import java.util.List;

public class IdColumnBuilder implements CursorObserver<ResourceId> {

    private final PendingSlot<ColumnView> result;
    private final List<String> ids = Lists.newArrayList();

    public IdColumnBuilder(PendingSlot<ColumnView> result) {
        this.result = result;
    }

    @Override
    public void onNext(ResourceId resourceId) {
        ids.add(resourceId.asString());
    }

    @Override
    public void done() {
        result.set(new StringArrayColumnView(ids));
    }

    public ColumnView get() {
        return result.get();
    }
}
