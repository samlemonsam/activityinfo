package org.activityinfo.model.type;

import com.google.gson.JsonElement;

public class ErrorType implements FieldType {

    public static final FieldTypeClass TYPE_CLASS = new FieldTypeClass() {
        @Override
        public String getId() {
            return "ERR";
        }

        @Override
        public FieldType createType() {
            throw new UnsupportedOperationException();
        }
    };

    public static final ErrorType INSTANCE = new ErrorType();

    private ErrorType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }

}
