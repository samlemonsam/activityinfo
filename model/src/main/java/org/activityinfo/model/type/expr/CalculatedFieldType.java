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
package org.activityinfo.model.type.expr;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.*;

import javax.annotation.Nullable;

import static org.activityinfo.json.Json.createObject;

/**
 * A Value Type that represents a value calculated from a symbolic expression,
 * such as "A + B"
 */
public class CalculatedFieldType implements ParametrizedFieldType {

    public static final ParametrizedFieldTypeClass TYPE_CLASS = new ParametrizedFieldTypeClass() {
        @Override
        public String getId() {
            return "calculated";
        }

        @Override
        public FieldType createType() {
            return new CalculatedFieldType();
        }

        @Override
        public FieldType deserializeType(JsonValue parametersObject) {
            JsonValue exprElement = parametersObject.get("formula");
            if(exprElement.isJsonNull()) {
                exprElement = parametersObject.get("expression");
            }
            if(exprElement.isJsonNull()) {
                return new CalculatedFieldType();
            } else if(exprElement.isJsonObject()) {
                return new CalculatedFieldType(exprElement.getString("value"));
            } else {
                return new CalculatedFieldType(exprElement.asString());
            }
        }
    };


    private String expression;

    public CalculatedFieldType() {
    }

    public CalculatedFieldType(String expression) {
        this.expression = expression;
    }

    @Nullable
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public CalculatedFieldType withFormula(String formula) {
        return new CalculatedFieldType(formula);
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitCalculated(this);
    }

    @Override
    public boolean isUpdatable() {
        return false;
    }

    @Override
    public JsonValue getParametersAsJson() {
        JsonValue object = createObject();
        if (expression != null) {
            object.put("formula", expression);
        }
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }

}
