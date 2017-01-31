package org.activityinfo.ui.client.component.table.filter;

import org.activityinfo.model.expr.*;
import org.activityinfo.model.type.number.Quantity;

/**
 * Created by yuriyz on 5/23/2016.
 */
public class VisitConstantsVisitor implements ExprVisitor {

    public String constantValueAsString(ConstantExpr node) {
        String value = "";
        if (node.getValue() instanceof Quantity) {
            value = Double.toString(((Quantity) node.getValue()).getValue());
        } else if (node.getValue() != null) {
            value = node.getValue().toString();
        }
        return value;
    }

    @Override
    public Object visitConstant(ConstantExpr node) {
        return null;
    }

    @Override
    public Object visitSymbol(SymbolExpr symbolExpr) {
        return null;
    }

    @Override
    public Object visitGroup(GroupExpr expr) {
        expr.getExpr().accept(this);
        return null;
    }

    @Override
    public Object visitCompoundExpr(CompoundExpr compoundExpr) {
        compoundExpr.getValue().accept(this);
        return null;
    }

    @Override
    public Object visitFunctionCall(FunctionCallNode functionCallNode) {
        for (ExprNode node : functionCallNode.getArguments()) {
            node.accept(this);
        }
        return null;
    }
}
