package org.activityinfo.model.formula;

public interface FormulaVisitor<T> {

    T visitConstant(ConstantNode node);

    T visitSymbol(SymbolNode symbolNode);

    T visitGroup(GroupNode expr);

    T visitCompoundExpr(CompoundExpr compoundExpr);

    T visitFunctionCall(FunctionCallNode functionCallNode);

}
