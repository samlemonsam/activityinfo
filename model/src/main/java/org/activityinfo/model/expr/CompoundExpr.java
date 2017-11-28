package org.activityinfo.model.expr;

import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.expr.eval.EvalContext;
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
public class CompoundExpr extends ExprNode {
    @Nonnull
    private final ExprNode value;

    @Nonnull
    private final SymbolExpr field;

    public CompoundExpr(@Nonnull ResourceId value, @Nonnull String field) {
        this.value = new SymbolExpr(value);
        this.field = new SymbolExpr(field);
    }
    
    public CompoundExpr(@Nonnull ExprNode value, @Nonnull SymbolExpr field) {
        this.value = value;
        this.field = field;
    }

    public CompoundExpr(@Nonnull ExprNode value, @Nonnull ResourceId field) {
        this.value = value;
        this.field = new SymbolExpr(field.asString());
    }

    public CompoundExpr(@Nonnull ExprNode value, @Nonnull SymbolExpr field, SourceRange range) {
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
        throw new ExprSyntaxException(asExpression());
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
        throw new ExprSyntaxException("Cannot resolve type of compound expression with base expression type: " + fieldType);
    }


    @Nonnull
    public ExprNode getValue() {
        return value;
    }

    @Nonnull
    public SymbolExpr getField() {
        return field;
    }

    @Override
    public String asExpression() {
        return value.asExpression() + "." + field.asExpression();
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitCompoundExpr(this);
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
