package org.activityinfo.model.type;

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
    public String getId() {
        return "null";
    }

    @Override
    public FieldType createType() {
        return this;
    }
}
