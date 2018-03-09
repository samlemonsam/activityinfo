/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.model.formula;

import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.formula.functions.*;
import org.junit.Test;

import static org.activityinfo.model.formula.Formulas.call;
import static org.activityinfo.model.formula.Formulas.symbol;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class FormulaParserTest {

    private static final FormulaFunction PLUS = FormulaFunctions.get("+");
    private static final FormulaFunction MINUS = FormulaFunctions.get("-");
    private static final FormulaFunction MUL = FormulaFunctions.get("*");
    private static final FormulaFunction DIV = FormulaFunctions.get("/");
    private static final FormulaFunction LT = FormulaFunctions.get("<");
    private static final FormulaFunction EQ = FormulaFunctions.get("==");
    private static final FormulaFunction GE = FormulaFunctions.get(">=");
    private static final FormulaFunction AND = FormulaFunctions.get("&&");
    private static final FormulaFunction OR = FormulaFunctions.get("||");

    public static final SymbolNode A = symbol("A");
    public static final SymbolNode B = symbol("B");
    public static final SymbolNode C = symbol("C");
    public static final SymbolNode D = symbol("D");
    public static final SymbolNode E = symbol("E");

    public static final ConstantNode TRUE = new ConstantNode(true);
    public static final ConstantNode FALSE = new ConstantNode(false);


    @Test
    public void parseSimple() {
        expect("1", new ConstantNode(1));
        expect("(1)", new GroupNode(new ConstantNode(1)));
        expect("1+2", new FunctionCallNode(PLUS,
                new ConstantNode(1),
                new ConstantNode(2)));
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
        expect("a.b", new CompoundExpr(new SymbolNode("a"), new SymbolNode("b")));
    }

    @Test(expected = FormulaSyntaxException.class)
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
                        new SymbolNode("a"),
                        new SymbolNode("b")),
                    new SymbolNode("c")));
    }
    
    @Test
    public void parseEqualsSign() {
        expect("true==false", new FunctionCallNode(
                EqualFunction.INSTANCE,
                new ConstantNode(true),
                new ConstantNode(false)));
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
                        new GroupNode(new FunctionCallNode(OR, FALSE, TRUE)),
                        TRUE)
        );
    }

    @Test
    public void parseNested() {
        expect("(1+2)/3",
                new FunctionCallNode(ArithmeticFunctions.DIVIDE,
                        new GroupNode(
                                new FunctionCallNode(ArithmeticFunctions.BINARY_PLUS,
                                        new ConstantNode(1),
                                        new ConstantNode(2))),
                        new ConstantNode(3)));
    }

    @Test
    public void parseStringLiteral() {
        expect("A=='Foo'",
            call(EQ, symbol("A"), new ConstantNode("Foo")));
    }

    @Test
    public void parseComparisons() {
        expect("A==B", new FunctionCallNode(EqualFunction.INSTANCE,
                new SymbolNode("A"),
                new SymbolNode("B")));
    }

    @Test
    public void parseQuotedSymbol() {
        expect("[Year of expenditure]", new SymbolNode("Year of expenditure"));
    }

    @Test
    public void parseFunctions() {
        expect("containsAll({f1},{v1})", new FunctionCallNode(ContainsAllFunction.INSTANCE,
                new SymbolNode("f1"),
                new SymbolNode("v1"))
        );
        expect("!containsAll({f1},{v1})", new FunctionCallNode(NotFunction.INSTANCE,
                new FunctionCallNode(ContainsAllFunction.INSTANCE,
                        new SymbolNode("f1"),
                        new SymbolNode("v1"))
        ));
    }

    @Test
    public void incorrectFunction() {
        FormulaException exception = expectError("FOO(A, B)");

    }
    
    @Test
    public void parseMax() {
        expect("max(A)",
                new FunctionCallNode(MaxFunction.INSTANCE, new SymbolNode("A")));
        
        expect("max(A, B)", 
                new FunctionCallNode(MaxFunction.INSTANCE, new SymbolNode("A"), new SymbolNode("B")));
        
        expect("min(A, B, C)",
                new FunctionCallNode(MinFunction.INSTANCE, 
                        new SymbolNode("A"),
                        new SymbolNode("B"),
                        new SymbolNode("C")));

    }

    @Test
    public void parseInvalidEquals() {
        expectError("A+=");
    }

    @Test
    public void invalidNumber() {
        FormulaException exception = null;
        try {
            FormulaParser.parse("A*B+\nC + 13.342342.2343");
        } catch (FormulaException e) {
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
          new FunctionCallNode(FormulaFunctions.get("/"),
              new FunctionCallNode(FormulaFunctions.get("*"),
                  new FunctionCallNode(FormulaFunctions.get("*"), new SymbolNode("Exp"), new SymbolNode("Alloc")),
                  new SymbolNode("InCostUnsp")),
              new ConstantNode(10000)));

    }

    @Test
    public void symbolSourceRefs() {
        FunctionCallNode call = (FunctionCallNode) FormulaParser.parse("[A] + [B]");
        assertThat(call.getSourceRange(), equalTo(new SourceRange(new SourcePos(0, 0), new SourcePos(0, 9))));
        assertThat(call.getArgument(1).getSourceRange(), equalTo(new SourceRange(new SourcePos(0, 6), new SourcePos(0, 9))));
    }

    @Test
    public void symbolSourceConstants() {
        FunctionCallNode call = (FunctionCallNode) FormulaParser.parse("1+3.456");
        assertThat(call.getSourceRange(), equalTo(new SourceRange(new SourcePos(0, 0), new SourcePos(0, 7))));
    }

    private void expect(String formula, FormulaNode expr) {
        assertEquals(expr, parse(formula));
    }

    private FormulaException expectError(String formula) {
        try {
            parse(formula);
            throw new AssertionError("Expected error");
        } catch(FormulaException e) {
            return e;
        }
    }

    private FormulaNode parse(String string) {
        System.out.println("Parsing [" + string + "]");
        FormulaLexer lexer = new FormulaLexer(string);
        FormulaParser parser = new FormulaParser(lexer);
        return parser.parse();
    }

}
