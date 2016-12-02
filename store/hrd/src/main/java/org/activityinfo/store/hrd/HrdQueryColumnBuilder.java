package org.activityinfo.store.hrd;

import com.google.common.collect.Lists;
import com.googlecode.objectify.cmd.Query;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;


class HrdQueryColumnBuilder implements ColumnQueryBuilder {

    private FormClass formClass;
    private List<CursorObserver<ResourceId>> idObservers = Lists.newArrayList();
    private List<FieldObserver> fieldObservers = Lists.newArrayList();
    private List<CursorObserver<FieldValue>> parentFieldObservers = null;
    private List<CursorObserver<?>> observers = Lists.newArrayList();

    HrdQueryColumnBuilder(FormClass formClass) {
        this.formClass = formClass;
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        idObservers.add(observer);
        observers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {

        FieldObserver fieldObserver;
        if(fieldId.equals(FormClass.PARENT_FIELD_ID)) {
            if(parentFieldObservers == null) {
                parentFieldObservers = new ArrayList<>();
            }
            parentFieldObservers.add(observer);

        } else {

            FormField field = formClass.getField(fieldId);
            FieldConverter converter = FieldConverters.forType(field.getType());
            fieldObserver = new FieldObserver(field.getName(), converter, observer);
            fieldObservers.add(fieldObserver);

        }

        observers.add(observer);
    }

    @Override
    public void execute() {

        Query<FormRecordEntity> query = ofy()
                .load()
                .type(FormRecordEntity.class)
                .ancestor(FormEntity.key(formClass));

        for (FormRecordEntity entity : query.iterable()) {

            for (CursorObserver<ResourceId> idObserver : idObservers) {
                idObserver.onNext(entity.getRecordId());
            }
            for (FieldObserver fieldObserver : fieldObservers) {
                fieldObserver.onNext(entity.getFieldValues());
            }
            if(parentFieldObservers != null) {
                ResourceId parentRecordId = ResourceId.valueOf(entity.getParentRecordId());
                RecordRef parentRef = new RecordRef(formClass.getParentFormId().get(), parentRecordId);
                ReferenceValue parent = new ReferenceValue(parentRef);
                for (CursorObserver<FieldValue> parentFieldObserver : parentFieldObservers) {
                    parentFieldObserver.onNext(parent);
                }
            }
        }
        
        for (CursorObserver<?> observer : observers) {
            observer.done();
        }
    }
}
