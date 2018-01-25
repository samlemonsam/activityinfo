package org.activityinfo.model.expr;

public interface ExprVisitor<T> {


    public T visitConstant(ConstantExpr node);

    public T visitSymbol(SymbolExpr symbolExpr);

    public T visitGroup(GroupExpr expr);

    public T visitCompoundExpr(CompoundExpr compoundExpr);

    public T visitFunctionCall(FunctionCallNode functionCallNode);

}
