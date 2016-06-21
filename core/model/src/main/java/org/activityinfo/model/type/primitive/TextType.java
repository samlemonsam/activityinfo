package org.activityinfo.model.type.primitive;

import com.google.gson.JsonElement;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

/**
 * A value type representing a single line of unicode text
 */
public class TextType implements FieldType {

    public static final FieldTypeClass TYPE_CLASS = new FieldTypeClass() {
        @Override
        public String getId() {
            return "FREE_TEXT";
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }
    };

    public static final TextType INSTANCE = new TextType();


    private TextType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement value) {
        return TextValue.valueOf(value.getAsString());
    }

    @Override
    public String toString() {
        return "TextType";
    }

    
    /**
     * Returns the singleton instance for serialization
     */
    TextType readResolve() {
        return INSTANCE;    
    }
}
