package org.activityinfo.model.formula;

import org.activityinfo.model.formula.functions.EqualFunction;
import org.activityinfo.model.formula.functions.IfFunction;
import org.activityinfo.model.formula.functions.MaxFunction;
import org.activityinfo.model.formula.functions.PlusFunction;
import org.activityinfo.model.formula.functions.date.DateFunction;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FunctionCallNodeTest {

    @Test
    public void binaryInfix() {
        assertThat(
            new FunctionCallNode(PlusFunction.INSTANCE, new SymbolNode("X"), new SymbolNode("Y")).asExpression(),
                equalTo("X + Y"));
    }


    @Test
    public void normalCall() {
        assertThat(
                new FunctionCallNode(MaxFunction.INSTANCE, new SymbolNode("X"), new SymbolNode("Y")).asExpression(),
                equalTo("max(X, Y)"));
    }

    @Test
    public void ifCall() {
        assertThat(
                new FunctionCallNode(IfFunction.INSTANCE,
                    new ConstantNode(true),
                    new ConstantNode(1),
                    new ConstantNode(0)).asExpression(),
                equalTo("if(true, 1, 0)")
        );
    }

    @Test
    public void dateCall() {
        assertThat(
                new FunctionCallNode(DateFunction.INSTANCE,
                        new ConstantNode(2017),
                        new ConstantNode(1),
                        new ConstantNode(2)).asExpression(),
                equalTo("DATE(2017, 1, 2)")
        );

        assertThat(
                new FunctionCallNode(DateFunction.INSTANCE,
                        new ConstantNode(2017),
                        new ConstantNode(1),
                        new ConstantNode(1)).asExpression(),
                equalTo("DATE(2017, 1, 1)")
        );
    }



    @Test
    public void equalsComparison() {
        assertThat(
                new FunctionCallNode(IfFunction.INSTANCE,
                    new FunctionCallNode(EqualFunction.INSTANCE, new ConstantNode(true), new ConstantNode(false)),
                    new ConstantNode(true),
                    new ConstantNode(false)).asExpression(),
                    equalTo("if(true == false, true, false)"));
    }

}