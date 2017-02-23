package org.activityinfo.store.mysql.cursor;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.FormStorage;

/**
 * Fetches a single form record using the ColumnSetBuilder
 */
public class RecordFetcher {
    
    private FormStorage collection;

    private class IdCollector implements CursorObserver<ResourceId> {

        private ResourceId value;
        
        @Override
        public void onNext(ResourceId value) {
            this.value = value;
        }

        @Override
        public void done() {
        }
    }
    
    private class FieldCollector implements CursorObserver<FieldValue> {
        private ResourceId fieldId;
        private FormRecord.Builder builder;
        private boolean set = false;

        public FieldCollector(ResourceId fieldId, FormRecord.Builder builder) {
            this.fieldId = fieldId;
            this.builder = builder;
        }

        @Override
        public void onNext(FieldValue value) {
            if(set) {
                throw new IllegalStateException(fieldId + ".onNext() called multiple times");
            }
            builder.setFieldValue(fieldId, value);
            set = true;
        }

        @Override
        public void done() {
        }
    }
    
    public RecordFetcher(FormStorage collection) {
        this.collection = collection;
    }

    public static Optional<FormRecord> fetch(FormStorage collection, ResourceId id) {
        RecordFetcher fetcher = new RecordFetcher(collection);
        return fetcher.get(id);
    }
    
    public Optional<FormRecord> get(ResourceId resourceId) {
        FormClass formClass = collection.getFormClass();
        FormRecord.Builder formRecord = FormRecord.builder();
        formRecord.setRecordId(resourceId);
        formRecord.setFormId(formClass.getId());

        IdCollector id = new IdCollector();

        ColumnQueryBuilder query = collection.newColumnQuery();
        query.addResourceId(id);
        query.only(resourceId);

        for (FormField formField : formClass.getFields()) {
            if(hasValues(formField)) {
                query.addField(formField.getId(), new FieldCollector(formField.getId(), formRecord));
            }
        }

        query.execute();

        if (id.value == null) {
            return Optional.absent();

        } else {
            return Optional.of(formRecord.build());
        }
    }

    private boolean hasValues(FormField formField) {
        if (formField.getType() instanceof CalculatedFieldType) {
            return false;
        } 
        if (formField.getType() instanceof SubFormReferenceType) {
            return false;
        }
        return true;
    }
}
