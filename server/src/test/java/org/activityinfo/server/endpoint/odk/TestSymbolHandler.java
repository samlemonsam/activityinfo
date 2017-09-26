package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.io.xform.xpath.XSymbolException;
import org.activityinfo.io.xform.xpath.XSymbolHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestSymbolHandler implements XSymbolHandler {

    private Map<String,String> symbolMap = new HashMap<>();

    public TestSymbolHandler(List<ResourceId> fieldIds) {
        Iterator<ResourceId> it = fieldIds.listIterator();
        while (it.hasNext()) {
            ResourceId id = it.next();
            Character domain = id.getDomain();
            if (domain.equals(CuidAdapter.ATTRIBUTE_DOMAIN)) {
                symbolMap.put(id.asString(), quote(id.asString()));
            } else {
                symbolMap.put(id.asString(), id.asString());
            }
        }
    }

    @Override
    public String resolveSymbol(String symbol) {
        return symbolMap.get(symbol);
    }

    @Override
    public String resolveSymbol(ExprNode exprNode) {
        if (exprNode instanceof SymbolExpr) {
            return resolveSymbol((SymbolExpr) exprNode);
        } else if (exprNode instanceof ConstantExpr) {
            return resolveSymbol((ConstantExpr) exprNode);
        }
        throw new XSymbolException(exprNode.asExpression());
    }

    private String resolveSymbol(SymbolExpr symbolExpr) throws XSymbolException {
        return resolveSymbol(symbolExpr.getName());
    }

    private String resolveSymbol(ConstantExpr constantExpr) throws XSymbolException {
        String resolved;
        if (constantExpr.getType() instanceof EnumType) {
            EnumValue enumValue = (EnumValue) constantExpr.getValue();
            resolved = resolveSymbol(enumValue.getValueId().asString());
        } else {
            resolved = resolveSymbol(constantExpr.getValue().toString());
        }
        return resolved;
    }

    private String quote(String string) {
        return "'" + string + "'";
    }

}
