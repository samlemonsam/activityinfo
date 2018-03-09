package org.activityinfo.model.formula;

import com.google.common.base.Function;
import org.activityinfo.model.formula.eval.EvalContext;
import org.activityinfo.model.formula.functions.Casting;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;


/**
 * Root of the formula Abstract Syntax Tree (AST). Formulas are used for validation and
 * calculation.
 */
public abstract class FormulaNode {

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

    public abstract <T> T accept(FormulaVisitor<T> visitor);

    public SourceRange getSourceRange() {
        return sourceRange;
    }

    public FormulaNode transform(Function<FormulaNode, FormulaNode> function) {
        return function.apply(this);
    }
}
