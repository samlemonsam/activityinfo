package org.activityinfo.model.expr;

import org.activityinfo.model.expr.functions.EqualFunction;
import org.activityinfo.model.expr.functions.IfFunction;
import org.activityinfo.model.expr.functions.MaxFunction;
import org.activityinfo.model.expr.functions.PlusFunction;
import org.activityinfo.model.expr.functions.date.DateFunction;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
                new FunctionCallNode(IfFunction.INSTANCE,
                    new ConstantExpr(true),
                    new ConstantExpr(1),
                    new ConstantExpr(0)).asExpression(),
                equalTo("if(true, 1, 0)")
        );
    }

    @Test
    public void dateCall() {
        assertThat(
                new FunctionCallNode(DateFunction.INSTANCE,
                        new ConstantExpr(2017),
                        new ConstantExpr(1),
                        new ConstantExpr(2)).asExpression(),
                equalTo("DATE(2017, 1, 2)")
        );

        assertThat(
                new FunctionCallNode(DateFunction.INSTANCE,
                        new ConstantExpr(2017),
                        new ConstantExpr(1),
                        new ConstantExpr(1)).asExpression(),
                equalTo("DATE(2017, 1, 1)")
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