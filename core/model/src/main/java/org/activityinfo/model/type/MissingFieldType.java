package org.activityinfo.model.type;

import com.google.gson.JsonElement;

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
    public FieldValue parseJsonValue(JsonElement value) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }

}
