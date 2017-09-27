package org.activityinfo.io.xform.xpath;

public class XSymbolException extends RuntimeException {

    public XSymbolException(String symbol) {
        super("Symbol: " + symbol + "could not be resolved.");
    }

    public XSymbolException(String symbol, Throwable cause) {
        super("Symbol: " + symbol + "could not be resolved.",cause);
    }
}
