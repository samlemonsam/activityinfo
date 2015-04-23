package org.activityinfo.model.expr;

import org.activityinfo.model.expr.eval.EvalContext;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

/**
 * An expression group ()
 */
public class GroupExpr extends ExprNode {

    private ExprNode expr;

    public GroupExpr(ExprNode expr) {
        super();
        this.expr = expr;
    }

    @Override
    public String toString() {
        return asExpression();
    }

    public String asExpression() {
        return "(" + expr.asExpression() + ")";
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitGroup(this);
    }

    @Override
    public FieldValue evaluate(EvalContext context) {
        return expr.evaluate(context);
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return expr.resolveType(context);
    }

    public ExprNode getExpr() {
        return expr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GroupExpr other = (GroupExpr) obj;
        return other.expr.equals(expr);
    }
}
