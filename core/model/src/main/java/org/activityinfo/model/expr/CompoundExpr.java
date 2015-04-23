package org.activityinfo.model.expr;

import org.activityinfo.model.expr.eval.EvalContext;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

import javax.annotation.Nonnull;

/**
 * symbol.symbol
 */
public class CompoundExpr extends ExprNode {
    @Nonnull
    private final ExprNode value;

    @Nonnull
    private final SymbolExpr field;

    public CompoundExpr(@Nonnull ExprNode value, @Nonnull SymbolExpr field) {
        this.value = value;
        this.field = field;
    }

    @Override
    public FieldValue evaluate(EvalContext context) {
        return null;
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return null;
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
}
