package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.*;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.hrd.entity.FormRecordEntity;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


class HrdQueryColumnBuilder implements ColumnQueryBuilder {

    private DatastoreService datastoreService;
    private Key collectionKey;
    private FormClass formClass;
    private List<CursorObserver<ResourceId>> idObservers = Lists.newArrayList();
    private List<FieldObserver> fieldObservers = Lists.newArrayList();
    private List<CursorObserver<?>> observers = Lists.newArrayList();

    HrdQueryColumnBuilder(DatastoreService datastoreService, Key collectionKey, FormClass formClass) {
        this.datastoreService = datastoreService;
        this.collectionKey = collectionKey;
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
            fieldObserver = new FieldObserver(FormClass.PARENT_FIELD_ID.asString(),
                    FieldConverters.forParentField(), observer);

        } else {

            FormField field = formClass.getField(fieldId);
            FieldConverter converter = FieldConverters.forType(field.getType());
            fieldObserver = new FieldObserver(field.getName(), converter, observer);
        }

        fieldObservers.add(fieldObserver);
        observers.add(observer);
    }

    @Override
    public void execute() throws IOException {

        Query query = new Query(FormRecordEntity.KIND, collectionKey);

        Transaction tx = datastoreService.beginTransaction();
        try {

            PreparedQuery preparedQuery = datastoreService.prepare(tx, query);
            Iterator<Entity> iterator = preparedQuery.asIterator();

            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                ResourceId id = ResourceId.valueOf(formClass.getId() + "-" + entity.getKey().getName());
                for (CursorObserver<ResourceId> idObserver : idObservers) {
                    idObserver.onNext(id);
                }
                for (FieldObserver fieldObserver : fieldObservers) {
                    fieldObserver.onNext(entity);
                }
            }

            for (CursorObserver<?> observer : observers) {
                observer.done();
            }
        } finally {
            tx.rollback();
        }
    }
}
