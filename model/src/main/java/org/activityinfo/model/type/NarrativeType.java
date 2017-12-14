package org.activityinfo.model.type;

import org.activityinfo.json.JsonValue;

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

    };

    private NarrativeType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        return NarrativeValue.valueOf(value.asString());
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitNarrative(this);
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