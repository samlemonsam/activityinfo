package org.activityinfo.model.expr.diagnostic;

@SuppressWarnings("GwtInconsistentSerializableClass")
public class AmbiguousSymbolException extends ExprException {


    public AmbiguousSymbolException(String symbol) {
        super("Ambiguous symbol [" + symbol + "]");
    }


}
