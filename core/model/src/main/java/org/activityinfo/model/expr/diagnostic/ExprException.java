package org.activityinfo.model.expr.diagnostic;

/**
 * Root Exception for all errors related to the evaluation of expressions
 */
public class ExprException extends RuntimeException {

    public ExprException() {
    }

    public ExprException(String message) {
        super(message);
    }
}
