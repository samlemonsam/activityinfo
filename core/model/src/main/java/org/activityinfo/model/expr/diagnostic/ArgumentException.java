package org.activityinfo.model.expr.diagnostic;

/**
 * Signals an invalid argument to a function.
 */
public class ArgumentException extends ExprException {

    private int argumentIndex;

    public ArgumentException(int argumentIndex, String message) {
        super(message);
        this.argumentIndex = argumentIndex;
    }

    public int getArgumentIndex() {
        return argumentIndex;
    }
}
