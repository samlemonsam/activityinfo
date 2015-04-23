package org.activityinfo.model.type.expr;

import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordFieldTypeClass;

/**
 * Value type that represents an expression
 */
public class ExprFieldType implements FieldType {

    public static final FieldTypeClass TYPE_CLASS = new RecordFieldTypeClass() {
        @Override
        public String getId() {
            return "expr";
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }

        @Override
        public FieldValue deserialize(Record record) {
            return ExprValue.fromRecord(record);
        }
    };

    public static final ExprFieldType INSTANCE = new ExprFieldType();


    private ExprFieldType() {
    }


    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

}
