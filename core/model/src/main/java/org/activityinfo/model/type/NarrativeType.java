package org.activityinfo.model.type;

import org.activityinfo.model.resource.Record;

/**
 * Value type that represents a FieldType containing paragraph-like text.
 *
 */
public class NarrativeType implements FieldType {


    public static final NarrativeType INSTANCE = new NarrativeType();

    public static final FieldTypeClass TYPE_CLASS = new RecordFieldTypeClass() {
        @Override
        public String getId() {
            return "NARRATIVE";
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }

        @Override
        public FieldValue deserialize(Record record) {
            return NarrativeValue.fromRecord(record);
        }
    };

    private NarrativeType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

}
