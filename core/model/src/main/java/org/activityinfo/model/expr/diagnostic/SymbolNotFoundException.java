package org.activityinfo.model.expr.diagnostic;

import org.activityinfo.model.expr.SymbolExpr;

public class SymbolNotFoundException extends ExprException {

    public SymbolNotFoundException() {
    }

    public SymbolNotFoundException(String symbol) {
        super("Could not resolve symbol [" + symbol + "]");
    }

    public SymbolNotFoundException(SymbolExpr symbolExpr) {
        this(symbolExpr.getName());
    }
}
