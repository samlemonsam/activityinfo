package org.activityinfo.model.type;

public class MissingFieldType implements FieldType {

    public static final MissingFieldType INSTANCE = new MissingFieldType();

    public static final FieldTypeClass TYPE_CLASS = new FieldTypeClass() {

        @Override
        public String getId() {
            return "missing";
        }

        @Override
        public FieldType createType() {
            return null;
        }
    };

    private MissingFieldType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }
}
