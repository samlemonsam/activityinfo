package org.activityinfo.model.expr;

import org.activityinfo.model.expr.functions.EqualFunction;
import org.activityinfo.model.expr.functions.IfFunction;
import org.activityinfo.model.expr.functions.MaxFunction;
import org.activityinfo.model.expr.functions.PlusFunction;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by alex on 8-12-16.
 */
public class FunctionCallNodeTest {

    @Test
    public void binaryInfix() {
        assertThat(
            new FunctionCallNode(PlusFunction.INSTANCE, new SymbolExpr("X"), new SymbolExpr("Y")).asExpression(),
                equalTo("X + Y"));
    }


    @Test
    public void normalCall() {
        assertThat(
                new FunctionCallNode(MaxFunction.INSTANCE, new SymbolExpr("X"), new SymbolExpr("Y")).asExpression(),
                equalTo("max(X, Y)"));
    }

    @Test
    public void ifCall() {
        assertThat(
                new FunctionCallNode(IfFunction.INSTANCE, new ConstantExpr(true), new ConstantExpr(1), new ConstantExpr(0)).asExpression(),
                equalTo("if(true, 1.0, 0.0)")
        );
    }


    @Test
    public void equalsComparison() {
        assertThat(
                new FunctionCallNode(IfFunction.INSTANCE,
                    new FunctionCallNode(EqualFunction.INSTANCE, new ConstantExpr(true), new ConstantExpr(false)),
                    new ConstantExpr(true),
                    new ConstantExpr(false)).asExpression(),
                    equalTo("if(true == false, true, false)"));
    }

}