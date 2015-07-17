package org.activityinfo.model.expr.eval;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

public class EmptyEvalContext implements EvalContext {

    public static final EmptyEvalContext INSTANCE = new EmptyEvalContext();

    private EmptyEvalContext() {

    }

    @Override
    public FieldValue resolveSymbol(String symbolName) {
        throw new UnsupportedOperationException(symbolName);
    }

    @Override
    public FieldType resolveSymbolType(String name) {
        throw new UnsupportedOperationException();
    }
}
