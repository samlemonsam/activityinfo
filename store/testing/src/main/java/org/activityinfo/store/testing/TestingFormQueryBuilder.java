package org.activityinfo.store.testing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestingFormQueryBuilder implements ColumnQueryBuilder {

    private final List<FormInstance> records;
    private final List<CursorObserver<ResourceId>> idObservers = new ArrayList<>();
    private final List<CursorObserver<FieldValue>> parentObservers = new ArrayList<>();
    private final Multimap<ResourceId, CursorObserver<FieldValue>> fieldObservers = HashMultimap.create();
    private final FormClass formClass;

    public TestingFormQueryBuilder(FormClass formClass, List<FormInstance> records) {
        this.records = records;
        this.formClass = formClass;
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
        if(fieldId.equals(FormClass.PARENT_FIELD_ID)) {
            if(!formClass.isSubForm()) {
                throw new IllegalStateException("Form " + formClass.getId() + " is not a sub form");
            }
            parentObservers.add(observer);
        } else {
            fieldObservers.put(fieldId, observer);
        }
    }

    @Override
    public void execute() {
        for (FormInstance record : records) {
            for (CursorObserver<ResourceId> idObserver : idObservers) {
                idObserver.onNext(record.getId());
            }
            for (CursorObserver<FieldValue> parentObserver : parentObservers) {
                parentObserver.onNext(new ReferenceValue(new RecordRef(formClass.getParentFormId().get(), record.getParentRecordId())));
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
        for (CursorObserver<FieldValue> parentObserver : parentObservers) {
            parentObserver.done();
        }
        for (CursorObserver<FieldValue> observer : fieldObservers.values()) {
            observer.done();
        }
    }
}
