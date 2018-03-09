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
package org.activityinfo.model.type;

import com.google.common.base.Strings;
import org.activityinfo.json.JsonValue;

import static org.activityinfo.json.Json.createObject;

/**
 * Field to which a sequential number is assigned when the record is first saved.
 *
 * <p>The Serial number can include an optional "prefix" which is calculated from the other
 * fields when the record is first created.</p>
 *
 */
public class SerialNumberType implements ParametrizedFieldType {

    public static final FieldTypeClass TYPE_CLASS = new ParametrizedFieldTypeClass() {

        @Override
        public String getId() {
            return "serial";
        }

        @Override
        public FieldType createType() {
            return new SerialNumberType();
        }

        @Override
        public FieldType deserializeType(JsonValue parametersObject) {
            SerialNumberType type = new SerialNumberType();
            if(parametersObject.hasKey("prefixFormula")) {
                type.prefixFormula = parametersObject.get("prefixFormula").asString();
            }
            if(parametersObject.hasKey("digits")) {
                type.digits = parametersObject.get("digits").asInt();
            }
            return type;
        }
    };

    private String prefixFormula;
    private int digits = 5;

    public SerialNumberType() {
    }

    public SerialNumberType(String prefixFormula, int digits) {
        this.prefixFormula = prefixFormula;
        this.digits = digits;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public SerialNumber parseJsonValue(JsonValue jsonElement) {
        if(jsonElement.isJsonPrimitive()) {
            JsonValue value = jsonElement;
            if (value.isNumber()) {
                return new SerialNumber(value.asInt());
            }
        } else if(jsonElement.isJsonObject()) {
            JsonValue jsonObject = jsonElement;
            return new SerialNumber(
                    jsonObject.get("prefix").asString(),
                    jsonObject.get("number").asInt());
        }

        throw new IllegalArgumentException();
    }

    /**
     * @return the formula used to compute the serial number prefix.
     */
    public String getPrefixFormula() {
        return prefixFormula;
    }

    public SerialNumberType withPrefixFormula(String formula) {
        return new SerialNumberType(formula, this.digits);
    }


    public boolean hasPrefix() {
        return !Strings.isNullOrEmpty(prefixFormula);
    }

    /**
     *
     * @return the number of digits to use when displaying the record number.
     */
    public int getDigits() {
        return digits;
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitSerialNumber(this);
    }

    @Override
    public boolean isUpdatable() {
        return false;
    }

    @Override
    public JsonValue getParametersAsJson() {
        JsonValue object = createObject();
        if(!Strings.isNullOrEmpty(prefixFormula)) {
            object.put("prefixFormula", prefixFormula);
        }
        object.put("digits", digits);
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }


    public String format(SerialNumber value) {
        String formatted = Strings.padStart(Integer.toString(value.getNumber()), digits, '0');
        if(value.hasPrefix()) {
            formatted = value.getPrefix() + "-" + formatted;
        }
        return formatted;
    }
}
