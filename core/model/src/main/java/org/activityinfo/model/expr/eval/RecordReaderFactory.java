package org.activityinfo.model.expr.eval;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordFieldTypeClass;
import org.activityinfo.model.type.TypeRegistry;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;

public class RecordReaderFactory implements FieldReaderFactory<Record> {

    @Override
    public FieldReader<Record> create(FormField field) {
        final String fieldName = field.getName();
        if (field.getType() instanceof TextType) {
            return new TextReader(fieldName);

        } else if (field.getType() instanceof BooleanType) {
            return new BooleanReader(fieldName);

        } else if (field.getType().getTypeClass() instanceof RecordFieldTypeClass) {
            return new RecordFieldReader(field);

        } else {
            throw new UnsupportedOperationException("Cannot create field reader for field type " + field.getType());
        }
    }

    private static class TextReader implements FieldReader<Record> {
        private final String fieldName;

        public TextReader(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public FieldValue readField(Record record) {
            return TextValue.valueOf(record.isString(fieldName));
        }

        @Override
        public FieldType getType() {
            return TextType.INSTANCE;
        }
    }

    private static class BooleanReader implements FieldReader<Record> {
        private final String fieldName;

        public BooleanReader(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public FieldValue readField(Record record) {
            Object value = record.get(fieldName);
            if (value instanceof Boolean) {
                return BooleanFieldValue.valueOf(value == Boolean.TRUE);
            } else {
                return null;
            }
        }

        @Override
        public FieldType getType() {
            return BooleanType.INSTANCE;
        }
    }

    private static class RecordFieldReader implements FieldReader<Record> {
        private final String fieldName;
        private final RecordFieldTypeClass typeClass;
        private final FieldType type;

        public RecordFieldReader(FormField field) {
            this.fieldName = field.getName();
            this.type = field.getType();
            this.typeClass =  (RecordFieldTypeClass) type.getTypeClass();
        }

        @Override
        public FieldValue readField(Record record) {
            return TypeRegistry.readField(record, fieldName, typeClass);
        }

        @Override
        public FieldType getType() {
            return type;
        }
    }
}
