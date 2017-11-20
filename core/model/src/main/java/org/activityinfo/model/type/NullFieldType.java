package org.activityinfo.model.type;

import org.activityinfo.json.JsonValue;

/**
 * Type of the empty value
 */
public final class NullFieldType implements FieldType, FieldTypeClass {

    public static final NullFieldType INSTANCE = new NullFieldType();
    
    private NullFieldType() {
    }
    
    @Override
    public FieldTypeClass getTypeClass() {
        return this;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        return NullFieldValue.INSTANCE;
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    @Override
    public String getId() {
        return "null";
    }

    @Override
    public FieldType createType() {
        return this;
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }

}
