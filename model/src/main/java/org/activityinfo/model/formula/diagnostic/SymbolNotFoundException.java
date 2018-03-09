package org.activityinfo.model.formula.diagnostic;

import org.activityinfo.model.formula.SymbolNode;

public class SymbolNotFoundException extends FormulaException {


    public SymbolNotFoundException() {
    }

    public SymbolNotFoundException(String symbol) {
        super("Could not resolve symbol [" + symbol + "]");
    }

    public SymbolNotFoundException(SymbolNode symbolNode) {
        this(symbolNode.getName());
    }

}
