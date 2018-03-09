package org.activityinfo.model.formula.diagnostic;

@SuppressWarnings("GwtInconsistentSerializableClass")
public class AmbiguousSymbolException extends FormulaException {


    public AmbiguousSymbolException(String symbol) {
        super("Ambiguous symbol [" + symbol + "]");
    }


    public AmbiguousSymbolException(String symbol, String message) {
        super("Ambiguous symbol [" + symbol + "]: " + message);
    }

}
