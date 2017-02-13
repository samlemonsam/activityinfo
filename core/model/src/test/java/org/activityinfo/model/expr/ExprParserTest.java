package org.activityinfo.model.expr;


import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.expr.functions.*;
import org.junit.Test;

import static org.activityinfo.model.expr.Exprs.call;
import static org.activityinfo.model.expr.Exprs.symbol;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class ExprParserTest {

    private static final ExprFunction PLUS = ExprFunctions.get("+");
    private static final ExprFunction MINUS = ExprFunctions.get("-");
    private static final ExprFunction MUL = ExprFunctions.get("*");
    private static final ExprFunction DIV = ExprFunctions.get("/");
    private static final ExprFunction LT = ExprFunctions.get("<");
    private static final ExprFunction EQ = ExprFunctions.get("==");
    private static final ExprFunction GE = ExprFunctions.get(">=");
    private static final ExprFunction AND = ExprFunctions.get("&&");
    private static final ExprFunction OR = ExprFunctions.get("||");

    public static final SymbolExpr A = symbol("A");
    public static final SymbolExpr B = symbol("B");
    public static final SymbolExpr C = symbol("C");
    public static final SymbolExpr D = symbol("D");
    public static final SymbolExpr E = symbol("E");

    public static final ConstantExpr TRUE = new ConstantExpr(true);
    public static final ConstantExpr FALSE = new ConstantExpr(false);


    @Test
    public void parseSimple() {
        expect("1", new ConstantExpr(1));
        expect("(1)", new GroupExpr(new ConstantExpr(1)));
        expect("1+2", new FunctionCallNode(PLUS,
                new ConstantExpr(1),
                new ConstantExpr(2)));
    }

    @Test
    public void orderOfOperations() {
        expect("A*B+C/D*E", call(PLUS,
                                call(MUL, A, B),
                                call(MUL,
                                    call(DIV, C, D),
                                        E)));

        // (+ (+ (+ A B) C) (* D E))
        expect("A+B+C+D*E",
            call(PLUS,
                call(PLUS,
                    call(PLUS, A, B),
                    C),
                call(MUL, D, E)));

        expect("A < B == C >= D",
            call(EQ,
                call(LT, A, B),
                call(GE, C, D)));

        expect("A && B || C && D",
            call(OR,
                call(AND, A, B),
                call(AND, C, D)));
    }
    
    @Test
    public void parseCompound() {
        expect("a.b", new CompoundExpr(new SymbolExpr("a"), new SymbolExpr("b")));
    }

    @Test(expected = ExprSyntaxException.class)
    public void parseMissingOperand() {
        parse("A+");
    }

    @Test
    public void unaryOperators() {
        expect("+A", call(PLUS, A));
        expect("-A", call(MINUS, A));
        expect("-A+C*D",
            call(PLUS,
                call(MINUS, A),
                call(MUL, C, D)));
    }


    @Test
    public void parseCompoundOp() {
        expect("a.b==c", new FunctionCallNode(
                EqualFunction.INSTANCE,
                    new CompoundExpr(
                        new SymbolExpr("a"), 
                        new SymbolExpr("b")),
                    new SymbolExpr("c")));
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
        expect("true", TRUE);
        expect("false", FALSE);
        expect("true&&false&&false",
                call(AND,
                    call(AND, TRUE, FALSE),
                    FALSE));
    }

    @Test
    public void group() {
        expect("(false||true)&&true",
                new FunctionCallNode(AND,
                        new GroupExpr(new FunctionCallNode(OR, FALSE, TRUE)),
                        TRUE)
        );
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
    public void parseStringLiteral() {
        expect("A=='Foo'",
            call(EQ, symbol("A"), new ConstantExpr("Foo")));
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
    public void incorrectFunction() {
        ExprException exception = expectError("FOO(A, B)");

    }
    
    @Test
    public void parseMax() {
        expect("max(A)",
                new FunctionCallNode(MaxFunction.INSTANCE, new SymbolExpr("A")));
        
        expect("max(A, B)", 
                new FunctionCallNode(MaxFunction.INSTANCE, new SymbolExpr("A"), new SymbolExpr("B")));
        
        expect("min(A, B, C)",
                new FunctionCallNode(MinFunction.INSTANCE, 
                        new SymbolExpr("A"), 
                        new SymbolExpr("B"), 
                        new SymbolExpr("C")));

    }

    @Test
    public void parseInvalidEquals() {
        expectError("A+=");
    }

    @Test
    public void invalidNumber() {
        ExprException exception = null;
        try {
            ExprParser.parse("A*B+\nC + 13.342342.2343");
        } catch (ExprException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertThat(exception.getSourceRange(),
                equalTo(new SourceRange(new SourcePos(1, 4),
                                        new SourcePos(1, 18))));
    }

    @Test
    public void parseCalc() {
        expect("{Exp}*{Alloc}*{InCostUnsp}/10000",
          new FunctionCallNode(ExprFunctions.get("/"),
              new FunctionCallNode(ExprFunctions.get("*"),
                  new FunctionCallNode(ExprFunctions.get("*"), new SymbolExpr("Exp"), new SymbolExpr("Alloc")),
                  new SymbolExpr("InCostUnsp")),
              new ConstantExpr(10000)));

    }

    @Test
    public void symbolSourceRefs() {
        FunctionCallNode call = (FunctionCallNode) ExprParser.parse("[A] + [B]");
        assertThat(call.getSourceRange(), equalTo(new SourceRange(new SourcePos(0, 0), new SourcePos(0, 9))));
        assertThat(call.getArgument(1).getSourceRange(), equalTo(new SourceRange(new SourcePos(0, 6), new SourcePos(0, 9))));
    }

    @Test
    public void symbolSourceConstants() {
        FunctionCallNode call = (FunctionCallNode) ExprParser.parse("1+3.456");
        assertThat(call.getSourceRange(), equalTo(new SourceRange(new SourcePos(0, 0), new SourcePos(0, 7))));
    }

    private void expect(String formula, ExprNode expr) {
        assertEquals(expr, parse(formula));
    }

    private ExprException expectError(String formula) {
        try {
            parse(formula);
            throw new AssertionError("Expected error");
        } catch(ExprException e) {
            return e;
        }
    }

    private ExprNode parse(String string) {
        System.out.println("Parsing [" + string + "]");
        ExprLexer lexer = new ExprLexer(string);
        ExprParser parser = new ExprParser(lexer);
        return parser.parse();
    }

}
