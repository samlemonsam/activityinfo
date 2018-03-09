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
package org.activityinfo.model.formula.simple;

import com.google.common.collect.Iterables;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.functions.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;


public class SimpleCondition {
    private ResourceId fieldId;
    private SimpleOperator operator;
    private FieldValue value;


    public SimpleCondition(ResourceId fieldId, SimpleOperator operator, FieldValue value) {
        this.fieldId = fieldId;
        this.operator = operator;
        this.value = value;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public SimpleOperator getOperator() {
        return operator;
    }

    public FieldValue getValue() {
        return value;
    }

    public ResourceId getEnumId() {
        return Iterables.getOnlyElement(((EnumValue) value).getResourceIds());
    }

    public SimpleCondition negate() {
        return new SimpleCondition(fieldId, negateOperator(), value);
    }

    private SimpleOperator negateOperator() {
        switch (operator) {
            case EQUALS:
                return SimpleOperator.NOT_EQUALS;
            case NOT_EQUALS:
                return SimpleOperator.EQUALS;
            case GREATER_THAN:
                return SimpleOperator.LESS_THAN_EQUAL;
            case GREATER_THAN_EQUAL:
                return SimpleOperator.LESS_THAN;
            case LESS_THAN:
                return SimpleOperator.GREATER_THAN_EQUAL;
            case LESS_THAN_EQUAL:
                return SimpleOperator.GREATER_THAN;
            case INCLUDES:
                return SimpleOperator.NOT_INCLUDES;
            case NOT_INCLUDES:
                return SimpleOperator.INCLUDES;
            default:
                throw new IllegalStateException();
        }
    }


    public FormulaNode toFormula() {
        switch (operator) {
            case EQUALS:
                return binaryFunction(EqualFunction.INSTANCE);
            case NOT_EQUALS:
                return binaryFunction(NotEqualFunction.INSTANCE);
            case GREATER_THAN:
                return binaryFunction(GreaterFunction.INSTANCE);
            case GREATER_THAN_EQUAL:
                return binaryFunction(GreaterOrEqualFunction.INSTANCE);
            case LESS_THAN:
                return binaryFunction(LessFunction.INSTANCE);
            case LESS_THAN_EQUAL:
                return binaryFunction(LessOrEqualFunction.INSTANCE);
            case INCLUDES:
                return new CompoundExpr(fieldId, getEnumId().asString());
            case NOT_INCLUDES:
                return new FunctionCallNode(NotFunction.INSTANCE,
                        new CompoundExpr(fieldId, getEnumId().asString()));
            default:
                throw new IllegalStateException();
        }
    }

    private FormulaNode binaryFunction(FormulaFunction function) {

        FormulaNode left  = new SymbolNode(fieldId);
        FormulaNode right;
        if(value instanceof EnumValue) {
            right = new ConstantNode(((EnumValue) value));
        } else if(value instanceof Quantity) {
            right = new ConstantNode(((Quantity) value));
        } else if(value instanceof TextValue) {
            right = new ConstantNode(((TextValue) value));
        } else if(value instanceof ReferenceValue) {
            right = new SymbolNode(((ReferenceValue) value).getOnlyReference().getRecordId());
        } else {
            throw new IllegalStateException("value: " + value);
        }

        return new FunctionCallNode(function, left, right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleCondition that = (SimpleCondition) o;

        if (!fieldId.equals(that.fieldId)) return false;
        if (operator != that.operator) return false;
        return value.equals(that.value);

    }

    @Override
    public int hashCode() {
        int result = fieldId.hashCode();
        result = 31 * result + operator.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleCondition{" +
                "fieldId=" + fieldId +
                ", operator=" + operator +
                ", value=" + value +
                '}';
    }

}
