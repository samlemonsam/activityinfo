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

                FormInstance record = new FormInstance(formId, id.values.get(row));
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
