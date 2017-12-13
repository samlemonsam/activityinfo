package org.activityinfo.model.type;

import org.activityinfo.json.JsonValue;

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

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUpdatable() {
        return false;
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }

}
