package org.activityinfo.store.testing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestingFormQueryBuilder implements ColumnQueryBuilder {

    private final List<FormInstance> records;
    private final List<CursorObserver<ResourceId>> idObservers = new ArrayList<>();
    private final Multimap<ResourceId, CursorObserver<FieldValue>> fieldObservers = HashMultimap.create();

    public TestingFormQueryBuilder(List<FormInstance> records) {
        this.records = records;
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        idObservers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        fieldObservers.put(fieldId, observer);
    }

    @Override
    public void execute() {
        for (FormInstance record : records) {
            for (CursorObserver<ResourceId> idObserver : idObservers) {
                idObserver.onNext(record.getId());
            }
            for (Map.Entry<ResourceId, CursorObserver<FieldValue>> field : fieldObservers.entries()) {
                ResourceId fieldId = field.getKey();
                FieldValue fieldValue = record.get(fieldId);
                CursorObserver<FieldValue> observer = field.getValue();
                observer.onNext(fieldValue);
            }
        }

        for (CursorObserver<ResourceId> observer : idObservers) {
            observer.done();
        }
        for (CursorObserver<FieldValue> observer : fieldObservers.values()) {
            observer.done();
        }
    }
}
