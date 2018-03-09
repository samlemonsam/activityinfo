package org.activityinfo.model.formula;

import com.google.common.base.Function;
import org.activityinfo.model.formula.eval.EvalContext;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

/**
 * An expression group ()
 */
public class GroupNode extends FormulaNode {

    private FormulaNode expr;

    public GroupNode(FormulaNode expr) {
        super();
        this.expr = expr;
    }

    public GroupNode(FormulaNode expr, SourceRange sourceRange) {
        super();
        this.expr = expr;
        this.sourceRange = sourceRange;
    }

    @Override
    public String toString() {
        return asExpression();
    }

    public String asExpression() {
        return "(" + expr.asExpression() + ")";
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitGroup(this);
    }

    @Override
    public FormulaNode transform(Function<FormulaNode, FormulaNode> function) {
        return function.apply(new GroupNode(expr.transform(function)));
    }

    @Override
    public FieldValue evaluate(EvalContext context) {
        return expr.evaluate(context);
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return expr.resolveType(context);
    }

    public FormulaNode getExpr() {
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
        GroupNode other = (GroupNode) obj;
        return other.expr.equals(expr);
    }
}
