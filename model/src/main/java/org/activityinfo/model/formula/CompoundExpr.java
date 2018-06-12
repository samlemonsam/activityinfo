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

import com.google.common.base.Function;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.formula.eval.EvalContext;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import javax.annotation.Nonnull;

/**
 * symbol.symbol
 */
public class CompoundExpr extends FormulaNode {
    @Nonnull
    private final FormulaNode value;

    @Nonnull
    private final SymbolNode field;

    public CompoundExpr(@Nonnull ResourceId value, @Nonnull String field) {
        this.value = new SymbolNode(value);
        this.field = new SymbolNode(field);
    }
    
    public CompoundExpr(@Nonnull FormulaNode value, @Nonnull SymbolNode field) {
        this.value = value;
        this.field = field;
    }

    public CompoundExpr(@Nonnull FormulaNode value, @Nonnull ResourceId field) {
        this.value = value;
        this.field = new SymbolNode(field.asString());
    }

    public CompoundExpr(@Nonnull FormulaNode value, @Nonnull SymbolNode field, SourceRange range) {
        this.value = value;
        this.field = field;
        this.sourceRange = range;
    }

    @Override
    public FieldValue evaluate(EvalContext context) {
        FieldValue baseValue = value.evaluate(context);
        if(baseValue instanceof EnumValue) {
            EnumValue enumValue = (EnumValue) baseValue;
            return evaluateEnumValue(enumValue);
        }
        return BooleanFieldValue.FALSE;
    }

    private FieldValue evaluateEnumValue(EnumValue enumValue) {
        for (ResourceId enumId : enumValue.getResourceIds()) {
            if(enumId.asString().equals(field.getName())) {
                return BooleanFieldValue.TRUE;
            }
        }
        return BooleanFieldValue.FALSE;
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        FieldType fieldType = value.resolveType(context);
        if(fieldType instanceof EnumType) {
            return BooleanType.INSTANCE;
        }
        throw new FormulaSyntaxException("Cannot resolve type of compound expression with base expression type: " + fieldType);
    }


    @Nonnull
    public FormulaNode getValue() {
        return value;
    }

    @Nonnull
    public SymbolNode getField() {
        return field;
    }

    @Override
    public String asExpression() {
        return value.asExpression() + "." + field.asExpression();
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitCompoundExpr(this);
    }

    @Override
    public FormulaNode transform(Function<FormulaNode, FormulaNode> function) {
        return function.apply(new CompoundExpr(
                value.transform(function),
                field));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompoundExpr that = (CompoundExpr) o;

        if (!field.equals(that.field)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + field.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return value + "." + field;
    }
}
