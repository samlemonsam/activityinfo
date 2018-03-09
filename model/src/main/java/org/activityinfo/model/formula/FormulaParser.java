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

import com.google.common.base.Strings;
import com.google.common.collect.PeekingIterator;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.formula.functions.FormulaFunction;
import org.activityinfo.model.formula.functions.FormulaFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Iterators.filter;
import static com.google.common.collect.Iterators.peekingIterator;
import static java.util.Collections.singletonList;

/**
 * Recursive descent parser for the ActivityInfo formula language.
 *
 */
public class FormulaParser {

    private PeekingIterator<Token> lexer;

    public FormulaParser(Iterator<Token> tokens) {
        this.lexer = peekingIterator(filter(tokens, token -> token.getType() != TokenType.WHITESPACE));
    }

    /**
     * Consume and return the next token if it is of type {@code tokenType}, or
     * throw an {@link FormulaSyntaxException} if there are no remaining tokens or
     * the next token is of the wrong type.
     * @param tokenType
     * @return
     */
    private Token expect(TokenType tokenType) {
        if(lexer.hasNext()) {
            Token token = lexer.next();
            if (token.getType() == tokenType) {
                return token;
            }
        }
        throw new FormulaSyntaxException("Expected " + tokenType);
    }

    private FormulaNode disjunction() {
        FormulaNode left = conjunction();
        while(lexer.hasNext() && lexer.peek().isOrOperator()) {
            FormulaFunction op = function();
            FormulaNode right = conjunction();

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }

    private FormulaNode conjunction() {
        FormulaNode left = equality();
        while(lexer.hasNext() && lexer.peek().isAndOperator()) {
            FormulaFunction op = function();
            FormulaNode right = equality();

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }

    private FormulaNode equality() {
        FormulaNode left = relational();
        while(lexer.hasNext() && lexer.peek().isEqualityOperator()) {
            FormulaFunction op = function();
            FormulaNode right = relational();

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }

    private FormulaNode relational() {
        FormulaNode left = term();
        while(lexer.hasNext() && lexer.peek().isRelationalOperator()) {
            FormulaFunction op = function();
            FormulaNode right = term();

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }

    private FormulaNode term() {

        // <term> ::= <factor> | <term> + <factor> | <term> - <factor>

        FormulaNode left = factor();
        while (lexer.hasNext() &&  lexer.peek().isAdditiveOperator()) {
            FormulaFunction op = function();
            FormulaNode right = factor();

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }


    public FormulaNode factor() {

        // <factor> ::= <unary> | <factor> * <unary> | <factor> / <unary>

        FormulaNode left = unary();
        while(lexer.hasNext() && lexer.peek().isMultiplicativeOperator()) {
            FormulaFunction function = function();
            FormulaNode right = unary();

            left = binaryInfixCall(function, left, right);
        }
        return left;
    }

    private FormulaNode unary() {
        // <unary> ::=  + <unary> | - <unary> | <unary2>

        if(!lexer.hasNext()) {
            throw new FormulaSyntaxException("Unexpected end of formula");
        }
        Token token = lexer.peek();
        if(token.getType() == TokenType.OPERATOR) {
            if(token.getString().equals("-") ||
               token.getString().equals("+")) {

                Token opToken = lexer.next();
                FormulaFunction op = function(opToken);
                FormulaNode operand = unary();
                SourceRange sourceRange = new SourceRange(opToken.getStart(), operand.getSourceRange().getEnd());
                return new FunctionCallNode(op, singletonList(operand), sourceRange);
            }
        }

        return unary2();
    }

    private FormulaNode unary2() {
        if(!lexer.hasNext()) {
            throw new FormulaSyntaxException("Unexpected end of formula");
        }
        Token token = lexer.peek();
        if(token.getType() == TokenType.OPERATOR) {
            if(token.getString().equals("!")) {
                Token opToken = lexer.next();
                FormulaFunction op = function(opToken);
                FormulaNode operand = primary();
                SourceRange sourceRange = new SourceRange(opToken.getStart(), operand.getSourceRange().getEnd());
                return new FunctionCallNode(op, singletonList(operand), sourceRange);
            }
        }
        return primary();
    }

    private FormulaNode primary() {
        switch (lexer.peek().getType()) {
            case SYMBOL:
                if(lexer.peek().getString().matches("t\\d{10}")) {
                    return enumLiteral();
                } else {
                    return symbolOrCall();
                }
            case NUMBER:
                return number();
            case BOOLEAN_LITERAL:
                return booleanLiteral();
            case STRING_LITERAL:
                return stringLiteral();
            case PAREN_START:
                Token openToken = lexer.next();
                FormulaNode e = expression();
                Token closeToken = expect(TokenType.PAREN_END);
                return new GroupNode(e, new SourceRange(openToken, closeToken));

            default:
                throw new FormulaSyntaxException(new SourceRange(lexer.peek()),
                        "Expected a symbol, a number, a string, or '('");
        }
    }

    private FormulaNode stringLiteral() {
        Token token = lexer.next();
        assert token.getType() == TokenType.STRING_LITERAL;
        return new ConstantNode(token.getString(), new SourceRange(token));
    }

    private FormulaNode enumLiteral() {
        Token token = lexer.next();
        assert token.getType() == TokenType.SYMBOL;
        return new ConstantNode(token, new SourceRange(token));
    }

    private FormulaNode booleanLiteral() {
        Token token = lexer.next();
        assert token.getType() == TokenType.BOOLEAN_LITERAL;
        boolean value = token.getString().toLowerCase().equals("true");
        SourceRange source = new SourceRange(token);
        return new ConstantNode(value, source);
    }

    private FormulaFunction function() {
        Token token = lexer.next();
        return function(token);
    }


    private FunctionCallNode binaryInfixCall(FormulaFunction op, FormulaNode left, FormulaNode right) {
        SourceRange source = new SourceRange(left.getSourceRange(), right.getSourceRange());
        return new FunctionCallNode(op, Arrays.asList(left, right), source);
    }


    private FormulaFunction function(Token token) {
        try {
            return FormulaFunctions.get(token.getString());
        } catch (UnsupportedOperationException e) {
            throw new FormulaSyntaxException(new SourceRange(token), "'" + token.getString() + "' is not a function.");
        }
    }


    private FormulaNode symbolOrCall() {
        Token symbolToken = lexer.next();
        assert symbolToken.getType() == TokenType.SYMBOL;

        if(lexer.hasNext() && lexer.peek().getType() == TokenType.PAREN_START) {
            return call(symbolToken);
        } else {
            return compound(symbol(symbolToken));
        }
    }

    private FormulaNode call(Token functionToken) {
        FormulaFunction function = function(functionToken);
        expect(TokenType.PAREN_START);

        List<FormulaNode> arguments = new ArrayList<>();

        while(true) {
            if (!lexer.hasNext()) {
                throw new FormulaSyntaxException("Unexpected end of formula");
            }
            TokenType nextToken = lexer.peek().getType();
            if (nextToken == TokenType.COMMA) {
                // Consume comma and parse next argument
                lexer.next();
                continue;
            }
            if (nextToken == TokenType.PAREN_END) {
                // consume paren and complete argument list
                Token closingParen = lexer.next();
                return new FunctionCallNode(function, arguments, new SourceRange(functionToken, closingParen));
            }

            // Otherwise parse the next argument
            arguments.add(expression());
        }
    }

    private FormulaNode compound(SymbolNode symbol) {
        FormulaNode result = symbol;

        while(lexer.hasNext() && lexer.peek().isDot()) {
            lexer.next();
            SymbolNode field = symbol();
            result = new CompoundExpr(result, field, new SourceRange(result.getSourceRange(), field.getSourceRange()));
        }
        return result;
    }

    private SymbolNode symbol() {
        Token token = lexer.next();
        return symbol(token);
    }

    private SymbolNode symbol(Token token) {
        assert token.getType() == TokenType.SYMBOL;
        return new SymbolNode(token);
    }

    private FormulaNode number() {
        Token token = lexer.next();
        double value;
        try {
            value = Double.parseDouble(token.getString());
        } catch (NumberFormatException e) {
            throw new FormulaSyntaxException(new SourceRange(token), "Invalid number '" + token.getString() + "': " +
                    e.getMessage());
        }
        return new ConstantNode(value, new SourceRange(token));
    }

    public FormulaNode expression() {
        // Start with the operator with the highest precedence
        return disjunction();
    }

    public FormulaNode parse() {
        FormulaNode expr = expression();
        if(lexer.hasNext()) {
            Token extraToken = lexer.next();
            throw new FormulaSyntaxException(new SourceRange(extraToken), "Missing an operator like + - / *, etc.");
        }
        return expr;
    }

    public static FormulaNode parse(String expression) {
        if(Strings.isNullOrEmpty(expression)) {
            return new ConstantNode((String)null);
        }
        FormulaLexer lexer = new FormulaLexer(expression);
        FormulaParser parser = new FormulaParser(lexer);
        return parser.parse();
    }
}
