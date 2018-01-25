package org.activityinfo.model.expr.diagnostic;

import org.activityinfo.model.expr.SourceRange;

public class ExprSyntaxException extends ExprException {

    public ExprSyntaxException() {
    }

    public ExprSyntaxException(String message) {
        super(message);
    }

    public ExprSyntaxException(SourceRange sourceRange, String message) {
        super(sourceRange, message);
    }
}
