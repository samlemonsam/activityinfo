package org.activityinfo.model.formula.diagnostic;

import org.activityinfo.model.formula.SourceRange;

public class FormulaSyntaxException extends FormulaException {

    public FormulaSyntaxException() {
    }

    public FormulaSyntaxException(String message) {
        super(message);
    }

    public FormulaSyntaxException(SourceRange sourceRange, String message) {
        super(sourceRange, message);
    }
}
