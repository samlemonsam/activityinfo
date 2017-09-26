package org.activityinfo.io.xform.xpath;


import org.activityinfo.model.expr.ExprNode;

public interface XSymbolHandler {

    public String resolveSymbol(String symbol) throws XSymbolException;

    public String resolveSymbol(ExprNode exprNode) throws XSymbolException;

}
