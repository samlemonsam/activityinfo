package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Preconditions;
import org.activityinfo.io.xform.xpath.XSymbolException;
import org.activityinfo.io.xform.xpath.XSymbolHandler;
import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdkSymbolHandler implements XSymbolHandler {

    private final Map<String, String> symbolMap = new HashMap<>();

    public OdkSymbolHandler(List<OdkField> fields) {
        for(OdkField field : fields) {
            symbolMap.put(field.getModel().getId().asString(), field.getAbsoluteFieldName());
            if(field.getModel().getType() instanceof EnumType) {
                EnumType type = (EnumType) field.getModel().getType();
                for (EnumItem item : type.getValues()) {
                    symbolMap.put(item.getId().asString(), quote(item.getId().asString()));
                }
            }
        }
    }

    private String quote(String value) {
        return "'" + value + "'";
    }

    @Override
    public String resolveSymbol(String symbol) throws XSymbolException {
        Preconditions.checkArgument(symbol != null, "Symbol cannot be null.");
        String resolved = symbolMap.get(symbol);
        if (resolved == null) {
            throw new XSymbolException(symbol);
        }
        return resolved;
    }

    @Override
    public String resolveSymbol(ExprNode exprNode) throws XSymbolException {
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

}
