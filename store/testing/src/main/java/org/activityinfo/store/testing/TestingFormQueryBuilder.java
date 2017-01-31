package org.activityinfo.store.testing;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;

import java.util.ArrayList;
import java.util.List;

public class TestingFormQueryBuilder implements ColumnQueryBuilder {

    private List<CursorObserver<?>> observers = new ArrayList<>();

    @Override
    public void only(ResourceId resourceId) {

    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        observers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        observers.add(observer);
    }

    @Override
    public void execute() {
        for (CursorObserver<?> observer : observers) {
            observer.done();
        }
    }
}
