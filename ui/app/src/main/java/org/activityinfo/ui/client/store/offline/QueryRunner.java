/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.IDBCursor;
import org.activityinfo.indexedb.IDBCursorCallback;
import org.activityinfo.indexedb.IDBTransaction;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class QueryRunner implements ColumnQueryBuilder {

    private static final Logger LOGGER = Logger.getLogger(QueryRunner.class.getName());


    private static class FieldObserver implements CursorObserver<RecordObject> {
        private String fieldName;
        private FieldType fieldType;
        private CursorObserver<FieldValue> observer;

        public FieldObserver(String fieldName, FieldType fieldType, CursorObserver<FieldValue> observer) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.observer = observer;
        }

        public void onNext(RecordObject record) {
            JsonValue jsonValue = record.getField(fieldName);
            if(jsonValue.isJsonNull()) {
                observer.onNext(null);
            } else {
                FieldValue value = fieldType.parseJsonValue(jsonValue);
                observer.onNext(value);
            }
        }

        @Override
        public void done() {
            observer.done();
        }
    }

    private static class ParentObserver implements CursorObserver<RecordObject> {

        private ResourceId parentFormId;
        private CursorObserver<FieldValue> observer;

        private ParentObserver(ResourceId parentFormId, CursorObserver<FieldValue> observer) {
            this.parentFormId = parentFormId;
            this.observer = observer;
        }


        @Override
        public void onNext(RecordObject value) {
            if(value.getParentRecordId() == null) {
                observer.onNext(null);
            } else {
                observer.onNext(new ReferenceValue(
                    new RecordRef(parentFormId, ResourceId.valueOf(value.getParentRecordId()))));
            }
        }

        @Override
        public void done() {
            observer.done();
        }
    }


    private FormClass formClass;
    private IDBTransaction tx;

    private List<CursorObserver<ResourceId>> idObservers = new ArrayList<>();
    private List<CursorObserver<RecordObject>> recordObservers = new ArrayList<>();

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

        if(fieldId.asString().equals("@parent")) {
            recordObservers.add(new ParentObserver(formClass.getParentFormId().get(), observer));

        } else {
            FormField field = formClass.getField(fieldId);
            FieldObserver fieldObserver = new FieldObserver(field.getName(), field.getType(), observer);
            recordObservers.add(fieldObserver);
        }
    }

    @Override
    public void execute() {
        tx.objectStore(RecordStore.DEF).openCursor(formClass.getId(), new IDBCursorCallback<RecordObject>() {
            @Override
            public void onNext(IDBCursor<RecordObject> cursor) {
                ResourceId resourceId = RecordStore.recordIdOf(cursor);

                for (CursorObserver<ResourceId> observer : idObservers) {
                    observer.onNext(resourceId);
                }

                RecordObject record = cursor.getValue();
                for (CursorObserver<RecordObject> fieldObserver : recordObservers) {
                    fieldObserver.onNext(record);
                }

                cursor.continue_();
            }

            @Override
            public void onDone() {
                for (CursorObserver<ResourceId> observer : idObservers) {
                    observer.done();
                }
                for (CursorObserver<RecordObject> fieldObserver : recordObservers) {
                    fieldObserver.done();
                }
            }
        });
    }
}
