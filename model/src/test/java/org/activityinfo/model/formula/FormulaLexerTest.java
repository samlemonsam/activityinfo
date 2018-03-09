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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormulaLexerTest {

    @Test
    public void simpleTokenizing() {
        expect("1+2",
                new Token(TokenType.NUMBER, 0, "1"),
                new Token(TokenType.OPERATOR, 1, "+"),
                new Token(TokenType.NUMBER, 2, "2"));

        expect("1 \n+ 2",
                new Token(TokenType.NUMBER, 0, "1"),
                new Token(TokenType.WHITESPACE, 1, " \n"),
                new Token(TokenType.OPERATOR, 0, "+"),
                new Token(TokenType.WHITESPACE, 1, " "),
                new Token(TokenType.NUMBER, 2, "2"));


        expect("((1+3)*(44323+455))/66   ",
                new Token(TokenType.PAREN_START, 0, "("),
                new Token(TokenType.PAREN_START, 1, "("),
                new Token(TokenType.NUMBER, 2, "1"),
                new Token(TokenType.OPERATOR, 3, "+"),
                new Token(TokenType.NUMBER, 4, "3"),
                new Token(TokenType.PAREN_END, 5, ")"),
                new Token(TokenType.OPERATOR, 6, "*"),
                new Token(TokenType.PAREN_START, 7, "("),
                new Token(TokenType.NUMBER, 8, "44323"),
                new Token(TokenType.OPERATOR, 13, "+"),
                new Token(TokenType.NUMBER, 14, "455"),
                new Token(TokenType.PAREN_END, 17, ")"),
                new Token(TokenType.PAREN_END, 18, ")"),
                new Token(TokenType.OPERATOR, 19, "/"),
                new Token(TokenType.NUMBER, 20, "66"),
                new Token(TokenType.WHITESPACE, 22, "   "));
    }

    @Test
    public void compareOperatorsTokenizing() {
        expect("2>1",
                new Token(TokenType.NUMBER, 0, "2"),
                new Token(TokenType.OPERATOR, 1, ">"),
                new Token(TokenType.NUMBER, 2, "1")
        );
        expect("4<5",
                new Token(TokenType.NUMBER, 0, "4"),
                new Token(TokenType.OPERATOR, 1, "<"),
                new Token(TokenType.NUMBER, 2, "5")
        );

        expect("6<=7",
                new Token(TokenType.NUMBER, 0, "6"),
                new Token(TokenType.OPERATOR, 1, "<="),
                new Token(TokenType.NUMBER, 3, "7")
        );
        expect("5<=5",
                new Token(TokenType.NUMBER, 0, "5"),
                new Token(TokenType.OPERATOR, 1, "<="),
                new Token(TokenType.NUMBER, 3, "5")
        );
    }

    @Test
    public void booleanTokenizing() {
        expect("true", new Token(TokenType.BOOLEAN_LITERAL, 0, "true"));
        expect("false", new Token(TokenType.BOOLEAN_LITERAL, 0, "false"));
        expect("true&&false",
                new Token(TokenType.BOOLEAN_LITERAL, 0, "true"),
                new Token(TokenType.OPERATOR, 4, "&&"),
                new Token(TokenType.BOOLEAN_LITERAL, 6, "false")
        );
        expect("(true||false)&&(false&&true)",
                new Token(TokenType.PAREN_START, 0, "("),
                new Token(TokenType.BOOLEAN_LITERAL, 1, "true"),
                new Token(TokenType.OPERATOR, 5, "||"),
                new Token(TokenType.BOOLEAN_LITERAL, 7, "false"),
                new Token(TokenType.PAREN_END, 12, ")"),
                new Token(TokenType.OPERATOR, 13, "&&"),
                new Token(TokenType.PAREN_START, 15, "("),
                new Token(TokenType.BOOLEAN_LITERAL, 16, "false"),
                new Token(TokenType.OPERATOR, 21, "&&"),
                new Token(TokenType.BOOLEAN_LITERAL, 23, "true"),
                new Token(TokenType.PAREN_END, 27, ")")
        );
    }

    @Test
    public void stringTokenizing() {
        expect("\"true\"", new Token(TokenType.STRING_LITERAL, 0, "true"));
        expect("\"1\"", new Token(TokenType.STRING_LITERAL, 0, "1"));
        expect("\"1a1\"", new Token(TokenType.STRING_LITERAL, 0, "1a1"));
    }

    @Test
    public void unescapedSymbolTokenizing() {
        expect("i1+i2",
                new Token(TokenType.SYMBOL, 0, "i1"),
                new Token(TokenType.OPERATOR, 2, "+"),
                new Token(TokenType.SYMBOL, 3, "i2")
        );
        expect("class1_i1+class2_i2",
                new Token(TokenType.SYMBOL, 0, "class1_i1"),
                new Token(TokenType.OPERATOR, 9, "+"),
                new Token(TokenType.SYMBOL, 10, "class2_i2")
        );
        expect("(class1_i1+class2_i2)/class3_i3",
                new Token(TokenType.PAREN_START, 0, "("),
                new Token(TokenType.SYMBOL, 1, "class1_i1"),
                new Token(TokenType.OPERATOR, 10, "+"),
                new Token(TokenType.SYMBOL, 11, "class2_i2"),
                new Token(TokenType.PAREN_END, 20, ")"),
                new Token(TokenType.OPERATOR, 21, "/"),
                new Token(TokenType.SYMBOL, 22, "class3_i3")
        );

        expect("s000002_i0009ls+s000002_i0009lt",
                new Token(TokenType.SYMBOL, 0, "s000002_i0009ls"),
                new Token(TokenType.OPERATOR, 15, "+"),
                new Token(TokenType.SYMBOL, 16, "s000002_i0009lt")
        );

        expect("s000002_i0009ls>=s000002_i0009lt",
                new Token(TokenType.SYMBOL, 0, "s000002_i0009ls"),
                new Token(TokenType.OPERATOR, 15, ">="),
                new Token(TokenType.SYMBOL, 17, "s000002_i0009lt")
        );
    }

    @Test
    public void symbolTokenizing() {
        expect("{i1}+{i2}",
                new Token(TokenType.SYMBOL, 0, "i1"),
                new Token(TokenType.OPERATOR, 4, "+"),
                new Token(TokenType.SYMBOL, 5, "i2"));
        expect("{class1_i1}+{class2_i2}",
                new Token(TokenType.SYMBOL, 0, "class1_i1"),
                new Token(TokenType.OPERATOR, 11, "+"),
                new Token(TokenType.SYMBOL, 12, "class2_i2"));

        expect("({class1_i1}+{class2_i2})/{class3_i3}",
                new Token(TokenType.PAREN_START, 0, "("),
                new Token(TokenType.SYMBOL, 1, "class1_i1"),
                new Token(TokenType.OPERATOR, 12, "+"),
                new Token(TokenType.SYMBOL, 13, "class2_i2"),
                new Token(TokenType.PAREN_END, 24, ")"),
                new Token(TokenType.OPERATOR, 25, "/"),
                new Token(TokenType.SYMBOL, 26, "class3_i3"));

        expect("{s000002_i0009ls}+{s000002_i0009lt}",
                new Token(TokenType.SYMBOL, 0, "s000002_i0009ls"),
                new Token(TokenType.OPERATOR, 17, "+"),
                new Token(TokenType.SYMBOL, 18, "s000002_i0009lt"));

        expect("{s000002_i0009ls}<{s000002_i0009lt}",
                new Token(TokenType.SYMBOL, 0, "s000002_i0009ls"),
                new Token(TokenType.OPERATOR, 17, "<"),
                new Token(TokenType.SYMBOL, 18, "s000002_i0009lt"));
    }

    @Test
    public void bracketedSymbols() {
        expect("[Year of expenditure]", new Token(TokenType.SYMBOL, 0, "Year of expenditure"));
    }

    @Test
    public void functionTokenizing() {
        expect("containsAll({f1},{v1})",
                new Token(TokenType.SYMBOL, 0, "containsAll"),
                new Token(TokenType.PAREN_START, 11, "("),
                new Token(TokenType.SYMBOL, 12, "f1"),
                new Token(TokenType.COMMA, 16, ","),
                new Token(TokenType.SYMBOL, 17, "v1"),
                new Token(TokenType.PAREN_END, 21, ")")
        );
    }

    private void expect(String string, Token... tokens) {
        System.out.println("Tokenizing [" + string + "]");
        FormulaLexer tokenizer = new FormulaLexer(string);
        int expectedIndex = 0;
        while (!tokenizer.isEndOfInput()) {
            String expectedString;
            Token expected;
            if(expectedIndex < tokens.length) {
                expected = tokens[expectedIndex];
                expectedString = expected.toString();
            } else {
                expected = null;
                expectedString = "<NOTHING>";
            }
            Token actual = tokenizer.next();
            System.out.println(String.format("Expected: %15s, got %s", expectedString, actual.toString()));
            if(expected != null) {
                assertEquals("tokenStart", expected.getTokenStartColumn(), actual.getTokenStartColumn());
                assertEquals("text", expected.getString(), actual.getString());
                assertEquals("type", expected.getType(), actual.getType());

                if (!expected.equals(actual)) {
                    System.err.println("Unexpected result!");
                    throw new AssertionError();
                }
            }
            expectedIndex ++;
        }
    }


}