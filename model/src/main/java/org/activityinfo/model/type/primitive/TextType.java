/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.model.type.primitive;

import com.google.common.base.Strings;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.*;

import static org.activityinfo.json.Json.createObject;

/**
 * A value type representing a single line of unicode text
 */
public class TextType implements ParametrizedFieldType {

    public static final FieldTypeClass TYPE_CLASS = new ParametrizedFieldTypeClass() {
        @Override
        public FieldType deserializeType(JsonValue parametersObject) {
            String inputMask = null;
            if(parametersObject != null) {
                if(parametersObject.hasKey("inputMask")) {
                    inputMask = parametersObject.get("inputMask").asString();
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
    public FieldValue parseJsonValue(JsonValue value) {
        if(value == null) {
            return null;
        }
        return TextValue.valueOf(value.asString());
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
    public JsonValue getParametersAsJson() {
        JsonValue object = createObject();
        if(inputMask != null) {
            object.put("inputMask", inputMask);
        }
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public String getInputMask() {
        return inputMask;
    }

    public boolean hasInputMask() {
        return inputMask != null;
    }
}
