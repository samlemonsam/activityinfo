package org.activityinfo.model.type.primitive;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.type.*;

/**
 * A value type representing a single line of unicode text
 */
public class TextType implements ParametrizedFieldType {

    public static final FieldTypeClass TYPE_CLASS = new ParametrizedFieldTypeClass() {
        @Override
        public FieldType deserializeType(JsonObject parametersObject) {
            String inputMask = null;
            if(parametersObject != null) {
                if(parametersObject.has("inputMask")) {
                    inputMask = parametersObject.get("inputMask").getAsString();
                }
            }
            return new TextType(inputMask);
        }

        @Override
        public String getId() {
            return "FREE_TEXT";
        }

        @Override
        public FieldType createType() {
            return SIMPLE;
        }
    };

    /**
     * A simple text type, with no input mask.
     */
    public static final TextType SIMPLE = new TextType();

    private String inputMask;

    private TextType() {
    }

    private TextType(String inputMask) {
        this.inputMask = Strings.emptyToNull(inputMask);
    }

    /**
     *
     * @return a new TextType with the given input mask.
     */
    public TextType withInputMask(String inputMask) {
        return new TextType(inputMask);
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement value) {
        if(value == null) {
            return null;
        }
        return TextValue.valueOf(value.getAsString());
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitText(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    @Override
    public String toString() {
        return "TextType";
    }


    @Override
    public JsonObject getParametersAsJson() {
        JsonObject object = new JsonObject();
        if(inputMask != null) {
            object.addProperty("inputMask", inputMask);
        }
        return object;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    public String getInputMask() {
        return inputMask;
    }
}
