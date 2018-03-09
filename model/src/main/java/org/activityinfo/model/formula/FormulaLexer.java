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

import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.google.gwt.regexp.shared.RegExp;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;

import java.util.List;

/**
 * Splits an expression string into a sequence of tokens
 */
public class FormulaLexer extends UnmodifiableIterator<Token> {

    public static final char DOUBLE_QUOTE = '"';
    public static final char SINGLE_QUOTE = '\'';
    private static final RegExp AT_LEAST_ONE_ALPHANUMS = RegExp.compile("^[a-zA-Z0-9]{1,}$");

    private String string;
    private int currentCharIndex;
    private int currentLineIndex;
    private int currentColumnIndex;

    private int currentTokenStart = 0;
    private SourcePos currentTokenStartPos = new SourcePos(0, 0);

    private static final String OPERATOR_CHARS = "+-/*&|=!<>";

    public FormulaLexer(String string) {
        assert string != null : "expr cannot be null";
        this.string = string;
    }


    /**
     * @return the current character within the string being processed
     */
    private char peekChar() {
        return string.charAt(currentCharIndex);
    }

    private char nextChar() {
        return consumeChar();
    }

    /**
     * Adds the current char to the current token
     */
    private char consumeChar() {
        char c = string.charAt(currentCharIndex);
        currentCharIndex++;
        if(c == '\n') {
            currentLineIndex ++;
            currentColumnIndex = 0;
        } else {
            currentColumnIndex ++;
        }
        return c;
    }

    private void consumeChars(int count) {
        while(count > 0) {
            consumeChar();
            count--;
        }
    }

    private Token finishToken(TokenType type) {
        return finishToken(type, string.substring(currentTokenStart, currentCharIndex));
    }


    private Token finishToken(TokenType type, String text) {
        int length = currentCharIndex - currentTokenStart;
        Token token = new Token(type, currentTokenStartPos, length, text);
        currentTokenStart = currentCharIndex;
        currentTokenStartPos = new SourcePos(currentLineIndex, currentColumnIndex);

        return token;
    }

    public List<Token> readAll() {
        List<Token> tokens = Lists.newArrayList();
        while (!isEndOfInput()) {
            tokens.add(next());
        }
        return tokens;
    }

    public boolean isEndOfInput() {
        return currentCharIndex >= string.length();
    }

    @Override
    public boolean hasNext() {
        return !isEndOfInput();
    }

    @Override
    public Token next() {
        char c = nextChar();
        if (c == '(') {
            return finishToken(TokenType.PAREN_START);

        } else if (c == ')') {
            return finishToken(TokenType.PAREN_END);

        } else if (c == '{') {
            return readQuotedToken(TokenType.SYMBOL, '}');

        } else if (c == '[') {
            return readQuotedToken(TokenType.SYMBOL, ']');

        } else if (c == ',') {
            return finishToken(TokenType.COMMA);

        } else if (c == '.') {
            return finishToken(TokenType.DOT);

        } else if (c == DOUBLE_QUOTE) {
            return readQuotedToken(TokenType.STRING_LITERAL, DOUBLE_QUOTE);

        } else if (c == SINGLE_QUOTE) {
            return readQuotedToken(TokenType.STRING_LITERAL, SINGLE_QUOTE);

        } else if (isWhitespace(c)) {
            return readWhitespace();

        } else if (isNumberPart(c)) {
            return readNumber();

        } else if (isOperator(c)) {
            return readOperator(c);

        } else if (isBooleanLiteral(c)) {
            return readBooleanLiteral(c);

        } else if (isSymbolStart(c)) {
            return readSymbol(TokenType.SYMBOL);

        } else {
            throw new RuntimeException("Symbol '" + c + "' is not supported");
        }
    }

    private boolean isOperator(char c) {
        return OPERATOR_CHARS.indexOf(c) != -1;
    }

    private Token readOperator(char c) {
        while (!isEndOfInput() && isOperator(peekChar())) {
            consumeChar();
        }
        return finishToken(TokenType.OPERATOR);
    }

    private boolean isSymbolStart(char c) {
        return c == '@' ||  c == '_' || Character.isLetter(c);
    }

    private boolean isSymbolChar(char c) {
        return c == '_' || isAlphabetic(c) || Character.isDigit(c);
    }

    private boolean isNumberPart(char c) {
        return Character.isDigit(c) || c == '.';
    }

    private boolean isBooleanLiteral(char c) {
        final int currentIndex = currentCharIndex - 1;
        if (c == 't' || c == 'T') {
            String trueLiteral = Boolean.TRUE.toString();
            int endIndex = currentIndex + trueLiteral.length();
            if (endIndex <= string.length()) {
                String literal = string.substring(currentIndex, endIndex);
                return trueLiteral.equalsIgnoreCase(literal);
            }
        } else if (c == 'f' || c == 'F') {
            String falseLiteral = Boolean.FALSE.toString();
            int endIndex = currentIndex + falseLiteral.length();
            if (endIndex <= string.length()) {
                String literal = string.substring(currentIndex, endIndex);
                return falseLiteral.equalsIgnoreCase(literal);
            }
        }
        return false;
    }

    private Token readWhitespace() {
        while (!isEndOfInput() && isWhitespace(peekChar())) {
            consumeChar();
        }
        return finishToken(TokenType.WHITESPACE);
    }

    private Token readNumber() {
        while (!isEndOfInput() && isNumberPart(peekChar())) {
            consumeChar();
        }
        return finishToken(TokenType.NUMBER);
    }

    private Token readSymbol(TokenType tokenType) {
        while (!isEndOfInput() && isSymbolChar(peekChar())) {
            consumeChar();
        }
        return finishToken(tokenType);
    }

    private Token readQuotedToken(TokenType type, char closingQuote) throws FormulaSyntaxException {
        while(true) {
            if (isEndOfInput()) {
                throw new FormulaSyntaxException("End of input reached while looking for closing '" + closingQuote + "");
            }
            if (nextChar() == closingQuote) {
                return finishToken(type, string.substring(currentTokenStart+1, currentCharIndex-1));
            }
        }
    }

    private Token readBooleanLiteral(char c) {
        if (c == 't' || c == 'T') {
            consumeChars(Boolean.TRUE.toString().length() - 1);
        } else {
            consumeChars(Boolean.FALSE.toString().length() - 1);
        }
        return finishToken(TokenType.BOOLEAN_LITERAL);
    }


    public static boolean isWhitespace(char ch) {
        //Character.isWhitespace()
        return Character.isSpace(ch);
    }

    public static boolean isAlphabetic(char s) {
        return AT_LEAST_ONE_ALPHANUMS.test("" + s);
    }
}
