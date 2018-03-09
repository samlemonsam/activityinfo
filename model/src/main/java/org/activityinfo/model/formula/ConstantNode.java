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
package org.activityinfo.model.formula;

import org.activityinfo.model.formula.eval.EvalContext;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;

import javax.annotation.Nonnull;

public class ConstantNode extends FormulaNode {

    @Nonnull
    private final FieldValue value;
    private final FieldType type;

    public ConstantNode(@Nonnull FieldValue value, FieldType type) {
        this.value = value;
        this.type = type;
    }

    public ConstantNode(double value) {
        this(new Quantity(value), new QuantityType());
    }

    public ConstantNode(boolean value) {
        this(BooleanFieldValue.valueOf(value), BooleanType.INSTANCE);
    }

    public ConstantNode(String value) {
        this(TextValue.valueOf(value), TextType.SIMPLE);
    }

    public ConstantNode(Quantity value) {
        this(value, new QuantityType());
    }

    public ConstantNode(TextValue value) {
        this(value, TextType.SIMPLE);
    }

    public ConstantNode(LocalDate localDate) {
        this(localDate, LocalDateType.INSTANCE);
    }

    public ConstantNode(EnumValue value){
        this(value, new EnumType());
    }

    public ConstantNode(Token token, SourceRange range) {
        // Enum constant from parser
        this(new EnumValue(ResourceId.valueOf(token.getString())));
        this.sourceRange = range;
    }

    public ConstantNode(boolean value, SourceRange source) {
        this(value);
        this.sourceRange = source;
    }

    public ConstantNode(double value, SourceRange sourceRange) {
        this(value);
        this.sourceRange = sourceRange;
    }


    public ConstantNode(String string, SourceRange sourceRange) {
        this(string);
        this.sourceRange = sourceRange;
    }

    public static ConstantNode valueOf(FieldValue value) {
        if(value instanceof TextValue) {
            return new ConstantNode(((TextValue) value).asString());
        } else if(value instanceof BooleanFieldValue) {
            return new ConstantNode(value == BooleanFieldValue.TRUE);
        } else if(value instanceof Quantity) {
            return new ConstantNode(value, new QuantityType());
        } else if (value instanceof EnumValue) {
            return new ConstantNode(value, new EnumType());
        } else {
            throw new IllegalArgumentException(value.getTypeClass().getId());
        }
    }

    @Override
    public FieldValue evaluate(EvalContext context) {
        return value;
    }

    @Nonnull
    public FieldValue getValue() {
        return value;
    }

    @Override
    public String asExpression() {
        if(value instanceof Quantity) {
            double doubleValue = ((Quantity) this.value).getValue();
            if(Math.floor(doubleValue) == doubleValue && !Double.isInfinite(doubleValue)) {
                return Integer.toString((int)doubleValue);
            } else {
                return "" + doubleValue;
            }
        } else if(value instanceof BooleanFieldValue) {
            return ((BooleanFieldValue) value).asBoolean() ? "true" : "false";
        } else if(value instanceof EnumValue) {
            return ((EnumValue) value).getValueId().asString();
        } else {
            // TODO: Escaping
            return "\"" + value + "\"";
        }
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitConstant(this);
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConstantNode that = (ConstantNode) o;

        if (!value.equals(that.value)) {
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
        return asExpression();
    }

    public FieldType getType() {
        return type;
    }
}
