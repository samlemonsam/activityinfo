package org.activityinfo.model.formula.diagnostic;

import org.activityinfo.model.formula.SourceRange;

/**
 * Root Exception for all errors related to the evaluation of expressions
 */
public class FormulaException extends RuntimeException {

    private SourceRange sourceRange;

    public FormulaException() {
    }

    public FormulaException(String message) {
        super(message);
    }

    public FormulaException(SourceRange sourceRange, String message) {
        super(message);
        this.sourceRange = sourceRange;
    }

    public SourceRange getSourceRange() {
        return sourceRange;
    }
}
