package org.activityinfo.model.expr;

import com.google.common.base.Function;
import org.activityinfo.model.expr.eval.EvalContext;
import org.activityinfo.model.expr.functions.Casting;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;


/**
 * Root of the expression hierarchy. Expressions are used for validation and
 * calculation by AI
 */
public abstract class ExprNode {

    SourceRange sourceRange;

    /**
     * Evaluates the expression to a real value.
     * @param context
     */
    public abstract FieldValue evaluate(EvalContext context);

    /**
     *
     * @return the FieldType of the expression node
     */
    public abstract FieldType resolveType(EvalContext context);

    public abstract String asExpression();

    public boolean evaluateAsBoolean(EvalContext context) {
        FieldValue fieldValue = evaluate(context);
        return Casting.toBoolean(fieldValue);
    }

    public abstract <T> T accept(ExprVisitor<T> visitor);

    public SourceRange getSourceRange() {
        return sourceRange;
    }

    public ExprNode transform(Function<ExprNode, ExprNode> function) {
        return function.apply(this);
    }
}
