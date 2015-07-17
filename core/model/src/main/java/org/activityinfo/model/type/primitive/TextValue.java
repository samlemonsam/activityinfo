package org.activityinfo.model.type.primitive;

import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

public class TextValue implements FieldValue, HasStringValue {

    private final String value;

    private TextValue(String value) {
        this.value = value;
    }

    /**
     * Returns a {@code TextValue} object, or {@code null} if
     * {@code value} is {@code null} or empty.
     */
    public static TextValue valueOf(String value) {
        if(value == null || value.isEmpty()) {
            return null;
        } else {
            return new TextValue(value);
        }
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TextType.TYPE_CLASS;
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TextValue textValue = (TextValue) o;

        if (!value.equals(textValue.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
