package org.activityinfo.model.expr.diagnostic;

import org.activityinfo.model.expr.SourceRange;

/**
 * Root Exception for all errors related to the evaluation of expressions
 */
public class ExprException extends RuntimeException {

    private SourceRange sourceRange;

    public ExprException() {
    }

    public ExprException(String message) {
        super(message);
    }

    public ExprException(SourceRange sourceRange, String message) {
        super(message);
        this.sourceRange = sourceRange;
    }

    public SourceRange getSourceRange() {
        return sourceRange;
    }
}
