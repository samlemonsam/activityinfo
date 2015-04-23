package org.activityinfo.model.expr;


import org.activityinfo.model.expr.functions.*;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExprParserTest {


    @Test
    public void parseSimple() {
        expect("1", new ConstantExpr(1));
        expect("(1)", new GroupExpr(new ConstantExpr(1)));
        expect("1+2", new FunctionCallNode(PlusFunction.INSTANCE,
                new ConstantExpr(1),
                new ConstantExpr(2)));
    }

    @Test
    public void parseCompound() {
        expect("a.b", new CompoundExpr(new SymbolExpr("a"), new SymbolExpr("b")));
    }

    @Test
    public void parseEqualsSign() {
        expect("true==false", new FunctionCallNode(
                EqualFunction.INSTANCE,
                new ConstantExpr(true),
                new ConstantExpr(false)));
    }

    @Test
    public void parseBooleanSimple() {
        expect("true", new ConstantExpr(true));
        expect("false", new ConstantExpr(false));
        expect("true&&false&&false", new FunctionCallNode(BooleanFunctions.AND,
                new ConstantExpr(true),
                new FunctionCallNode(BooleanFunctions.AND,
                        new ConstantExpr(false),
                        new ConstantExpr(false)
                )
        ));
    }


    @Test
    public void parseNested() {
        expect("(1+2)/3",
                new FunctionCallNode(ArithmeticFunctions.DIVIDE,
                        new GroupExpr(
                                new FunctionCallNode(ArithmeticFunctions.BINARY_PLUS,
                                        new ConstantExpr(1),
                                        new ConstantExpr(2))),
                        new ConstantExpr(3)));
    }

    @Test
    public void parseSymbols() {
        expect("{i1}+{i2}+1", new FunctionCallNode(ArithmeticFunctions.BINARY_PLUS,
                new SymbolExpr("i1"),
                new FunctionCallNode(ArithmeticFunctions.BINARY_PLUS, new SymbolExpr("i2"),
                        new ConstantExpr(1))));

        expect("({class1_i1}+{class2_i2})/{class3_i3}",
                new FunctionCallNode(ArithmeticFunctions.DIVIDE,
                        new GroupExpr(
                                new FunctionCallNode(ArithmeticFunctions.BINARY_PLUS,
                                        new SymbolExpr("class1_i1"),
                                        new SymbolExpr("class2_i2"))
                        ), new SymbolExpr("class3_i3")));

        expect("{s000002_i0009ls}+{s000002_i0009lt}",
                new FunctionCallNode(ArithmeticFunctions.BINARY_PLUS,
                        new SymbolExpr("s000002_i0009ls"),
                        new SymbolExpr("s000002_i0009lt"))
        );
    }

    @Test
    public void parseComparisons() {
        expect("A==B", new FunctionCallNode(EqualFunction.INSTANCE,
                new SymbolExpr("A"),
                new SymbolExpr("B")));
    }

    @Test
    public void parseQuotedSymbol() {
        expect("[Year of expenditure]", new SymbolExpr("Year of expenditure"));
    }

    @Test
    public void parseFunctions() {
        expect("containsAll({f1},{v1})", new FunctionCallNode(ContainsAllFunction.INSTANCE,
                new SymbolExpr("f1"),
                new SymbolExpr("v1"))
        );
        expect("!containsAll({f1},{v1})", new FunctionCallNode(NotFunction.INSTANCE,
                new FunctionCallNode(ContainsAllFunction.INSTANCE,
                        new SymbolExpr("f1"),
                        new SymbolExpr("v1"))
        ));
    }

    @Test
    @Ignore("todo")
    public void parseCalc() {
        expect("{Exp}*{Alloc}*{InCostUnsp}/10000",
          new FunctionCallNode(ExprFunctions.get("/"),
              new FunctionCallNode(ExprFunctions.get("*"),
                  new FunctionCallNode(ExprFunctions.get("*"), new SymbolExpr("Exp"), new SymbolExpr("Alloc")),
                  new SymbolExpr("InCostUnsp")),
              new ConstantExpr(10000)));

    }


    private void expect(String string, ExprNode expr) {
        System.out.println("Parsing [" + string + "]");
        ExprLexer lexer = new ExprLexer(string);
        ExprParser parser = new ExprParser(lexer);
        ExprNode actual = parser.parse();

        assertEquals(expr, actual);
    }

}
