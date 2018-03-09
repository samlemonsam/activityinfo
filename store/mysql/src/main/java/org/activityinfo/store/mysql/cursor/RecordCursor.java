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
package org.activityinfo.store.mysql.cursor;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.spi.Cursor;
import org.activityinfo.store.spi.CursorObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecordCursor {


    private class FieldCollector implements CursorObserver<FieldValue> {

        private final ResourceId fieldId;
        private final List<FieldValue> values = new ArrayList<>();

        public FieldCollector(ResourceId fieldId) {
            this.fieldId = fieldId;
        }

        @Override
        public void onNext(FieldValue value) {
            values.add(value);
        }

        @Override
        public void done() {
        }
    }

    private class KeyCollector implements CursorObserver<ResourceId> {

        private final List<ResourceId> values = new ArrayList<>();

        @Override
        public void onNext(ResourceId value) {
            values.add(value);
        }

        @Override
        public void done() {
        }
    }

    private final ResourceId formId;
    private final TableMapping tableMapping;
    private final QueryExecutor executor;
    private final MySqlCursorBuilder builder;

    private KeyCollector id = new KeyCollector();
    private List<FieldCollector> fields = new ArrayList<>();

    public RecordCursor(TableMapping tableMapping, QueryExecutor executor) {

        this.formId = tableMapping.getFormClass().getId();
        this.tableMapping = tableMapping;
        this.executor = executor;
        this.builder = new MySqlCursorBuilder(tableMapping, executor);

        builder.addResourceId(id);
        for (FormField formField : tableMapping.getFormClass().getFields()) {
            FieldCollector fieldCollector = new FieldCollector(formField.getId());
            builder.addField(formField.getId(), fieldCollector);
            fields.add(fieldCollector);
        }
    }

    public Iterator<FormInstance> execute() {
        Cursor cursor = builder.open();
        while(cursor.next()) { }

        return new Iterator<FormInstance>() {

            private int row = 0;

            @Override
            public boolean hasNext() {
                return row < id.values.size();
            }

            @Override
            public FormInstance next() {

                FormInstance record = new FormInstance(id.values.get(row), formId);
                for (FieldCollector field : fields) {
                    record.set(field.fieldId, field.values.get(row));
                }

                row++;

                return record;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
