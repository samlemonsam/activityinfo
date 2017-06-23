package org.activityinfo.ui.client.store.offline;

import com.google.gson.JsonElement;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class QueryRunner implements ColumnQueryBuilder {

    private static final Logger LOGGER = Logger.getLogger(QueryRunner.class.getName());

    private static class FieldObserver {
        private String fieldName;
        private FieldType fieldType;
        private CursorObserver<FieldValue> observer;

        public FieldObserver(String fieldName, FieldType fieldType, CursorObserver<FieldValue> observer) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.observer = observer;
        }

        public void onNext(FormRecord record) {

            JsonElement jsonValue = record.getFields().get(fieldName);
            if(jsonValue == null) {
                observer.onNext(null);
            } else {
                FieldValue value = fieldType.parseJsonValue(jsonValue);
                observer.onNext(value);
            }
        }

        public void onDone() {
            observer.done();
        }
    }


    private FormClass formClass;
    private IDBTransaction tx;

    private List<CursorObserver<ResourceId>> idObservers = new ArrayList<>();
    private List<FieldObserver> fieldObservers = new ArrayList<>();

    public QueryRunner(FormClass formClass, IDBTransaction tx) {
        this.formClass = formClass;
        this.tx = tx;
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        idObservers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        FormField field = formClass.getField(fieldId);
        FieldObserver fieldObserver = new FieldObserver(field.getName(), field.getType(), observer);
        fieldObservers.add(fieldObserver);
    }

    @Override
    public void execute() {
        tx.records().openCursor(formClass.getId(), new RecordStore.RecordCursorCallback() {
            @Override
            public void onNext(RecordStore.RecordCursor cursor) {
                ResourceId resourceId = cursor.getRecordId();

                for (CursorObserver<ResourceId> observer : idObservers) {
                    observer.onNext(resourceId);
                }

                FormRecord record = cursor.getValue();
                for (FieldObserver fieldObserver : fieldObservers) {
                    fieldObserver.onNext(record);
                }

                cursor.continue_();
            }

            @Override
            public void onDone() {
                for (CursorObserver<ResourceId> observer : idObservers) {
                    observer.done();
                }
                for (FieldObserver fieldObserver : fieldObservers) {
                    fieldObserver.onDone();
                }
            }
        });
    }
}
