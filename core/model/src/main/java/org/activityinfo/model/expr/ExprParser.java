package org.activityinfo.model.expr;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.expr.functions.*;

import java.util.*;

/**
 * Recursive descent parser for the ActivityInfo formula language.
 *
 */
public class ExprParser {

    public static final Set<String> FUNCTIONS = Collections.unmodifiableSet(Sets.newHashSet(
            ContainsAllFunction.INSTANCE.getId(),
            ContainsAnyFunction.INSTANCE.getId(),
            NotContainsAllFunction.INSTANCE.getId(),
            NotContainsAnyFunction.INSTANCE.getId()
    ));

    private PeekingIterator<Token> lexer;

    public ExprParser(Iterator<Token> tokens) {
        this.lexer = Iterators.peekingIterator(Iterators.filter(tokens, new Predicate<Token>() {

            @Override
            public boolean apply(Token token) {
                return token.getType() != TokenType.WHITESPACE;
            }
        }));
    }

    /**
     * Consume and return the next token if it is of type {@code tokenType}, or
     * throw an {@link ExprSyntaxException} if there are no remaining tokens or
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
        throw new ExprSyntaxException("Expected " + tokenType);
    }

    private ExprNode disjunction() {
        ExprNode left = conjunction();
        while(lexer.hasNext() && lexer.peek().isOrOperator()) {
            ExprFunction op = function();
            ExprNode right = conjunction();

            left = new FunctionCallNode(op, left, right);
        }
        return left;
    }

    private ExprNode conjunction() {
        ExprNode left = equality();
        while(lexer.hasNext() && lexer.peek().isAndOperator()) {
            ExprFunction op = function();
            ExprNode right = equality();

            left = new FunctionCallNode(op, left, right);
        }
        return left;
    }

    private ExprNode equality() {
        ExprNode left = relational();
        while(lexer.hasNext() && lexer.peek().isEqualityOperator()) {
            ExprFunction op = function();
            ExprNode right = relational();

            left = new FunctionCallNode(op, left, right);
        }
        return left;
    }

    private ExprNode relational() {
        ExprNode left = term();
        while(lexer.hasNext() && lexer.peek().isRelationalOperator()) {
            ExprFunction op = function();
            ExprNode right = term();

            left = new FunctionCallNode(op, left, right);
        }
        return left;
    }

    private ExprNode term() {

        // <term> ::= <factor> | <term> + <factor> | <term> - <factor>

        ExprNode left = factor();
        while (lexer.hasNext() &&  lexer.peek().isAdditiveOperator()) {
            ExprFunction op = function();
            ExprNode right = factor();

            left = new FunctionCallNode(op, left, right);
        }
        return left;
    }


    public ExprNode factor() {

        // <factor> ::= <unary> | <factor> * <unary> | <factor> / <unary>

        ExprNode left = unary();
        while(lexer.hasNext() && lexer.peek().isMultiplicativeOperator()) {
            ExprFunction function = function();
            ExprNode right = unary();

            left = new FunctionCallNode(function, left, right);
        }
        return left;
    }

    private ExprNode unary() {
        // <unary> ::=  + <unary> | - <unary> | <unary2>

        if(!lexer.hasNext()) {
            throw new ExprSyntaxException("Unexpected end of formula");
        }
        Token token = lexer.peek();
        if(token.getType() == TokenType.OPERATOR) {
            if(token.getString().equals("-") ||
               token.getString().equals("+")) {

                ExprFunction op = function();
                ExprNode operand = unary();
                return new FunctionCallNode(op, operand);
            }
        }

        return unary2();
    }

    private ExprNode unary2() {
        if(!lexer.hasNext()) {
            throw new ExprSyntaxException("Unexpected end of formula");
        }
        Token token = lexer.peek();
        if(token.getType() == TokenType.OPERATOR) {
            if(token.getString().equals("!")) {
                ExprFunction op = function();
                ExprNode operand = primary();
                return new FunctionCallNode(op, operand);
            }
        }
        return primary();
    }

    private ExprNode primary() {
        switch (lexer.peek().getType()) {
            case SYMBOL:
                return symbolOrCall();
            case NUMBER:
                return number();
            case BOOLEAN_LITERAL:
                return booleanLiteral();
            case PAREN_START:
                lexer.next();
                ExprNode e = term();
                expect(TokenType.PAREN_END);
                return new GroupExpr(e);
            default:
                throw new ExprSyntaxException("factor: syntax error");
        }
    }


    private ExprNode booleanLiteral() {
        Token token = lexer.next();
        assert token.getType() == TokenType.BOOLEAN_LITERAL;
        return new ConstantExpr(token.getString().toLowerCase().equals("true"));
    }

    private ExprFunction function() {
        Token token = lexer.next();
        return function(token);
    }

    private ExprFunction function(Token token) {
        return ExprFunctions.get(token.getString());
    }


    private ExprNode symbolOrCall() {
        Token symbolToken = lexer.next();
        assert symbolToken.getType() == TokenType.SYMBOL;

        if(lexer.hasNext() && lexer.peek().getType() == TokenType.PAREN_START) {
            return call(symbolToken);
        } else {
            return compound(symbol(symbolToken));
        }
    }

    private ExprNode call(Token functionToken) {
        ExprFunction function = function(functionToken);
        expect(TokenType.PAREN_START);

        List<ExprNode> arguments = new ArrayList<>();

        while(true) {
            if (!lexer.hasNext()) {
                throw new ExprSyntaxException("Unexpected end of formula");
            }
            TokenType nextToken = lexer.peek().getType();
            if (nextToken == TokenType.COMMA) {
                // Consume comma and parse next argument
                lexer.next();
                continue;
            }
            if (nextToken == TokenType.PAREN_END) {
                // consume paren and complete argument list
                lexer.next();
                break;
            }

            // Otherwise parse the next argument
            arguments.add(parse());
        }

        return new FunctionCallNode(function, arguments);
    }

    private ExprNode compound(SymbolExpr symbol) {
        ExprNode result = symbol;

        while(lexer.hasNext() && lexer.peek().isDot()) {
            lexer.next();
            SymbolExpr field = symbol();
            result = new CompoundExpr(symbol, field);
        }
        return result;
    }

    private SymbolExpr symbol() {
        Token token = lexer.next();
        return symbol(token);
    }

    private SymbolExpr symbol(Token token) {
        assert token.getType() == TokenType.SYMBOL;
        return new SymbolExpr(token.getString());
    }

    private ExprNode number() {
        Token token = lexer.next();
        return new ConstantExpr(Double.parseDouble(token.getString()));
    }

    public ExprNode parse() {
        // Start with the operator with the highest precedence
        return disjunction();
    }

    public static ExprNode parse(String expression) {
        ExprLexer lexer = new ExprLexer(expression);
        ExprParser parser = new ExprParser(lexer);
        return parser.parse();
    }
}
