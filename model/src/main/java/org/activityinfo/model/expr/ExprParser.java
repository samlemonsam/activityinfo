package org.activityinfo.model.expr;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.expr.functions.*;
import org.activityinfo.model.type.NullFieldType;
import org.activityinfo.model.type.NullFieldValue;

import java.util.*;

import static java.util.Collections.singletonList;

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

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }

    private ExprNode conjunction() {
        ExprNode left = equality();
        while(lexer.hasNext() && lexer.peek().isAndOperator()) {
            ExprFunction op = function();
            ExprNode right = equality();

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }

    private ExprNode equality() {
        ExprNode left = relational();
        while(lexer.hasNext() && lexer.peek().isEqualityOperator()) {
            ExprFunction op = function();
            ExprNode right = relational();

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }

    private ExprNode relational() {
        ExprNode left = term();
        while(lexer.hasNext() && lexer.peek().isRelationalOperator()) {
            ExprFunction op = function();
            ExprNode right = term();

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }

    private ExprNode term() {

        // <term> ::= <factor> | <term> + <factor> | <term> - <factor>

        ExprNode left = factor();
        while (lexer.hasNext() &&  lexer.peek().isAdditiveOperator()) {
            ExprFunction op = function();
            ExprNode right = factor();

            left = binaryInfixCall(op, left, right);
        }
        return left;
    }


    public ExprNode factor() {

        // <factor> ::= <unary> | <factor> * <unary> | <factor> / <unary>

        ExprNode left = unary();
        while(lexer.hasNext() && lexer.peek().isMultiplicativeOperator()) {
            ExprFunction function = function();
            ExprNode right = unary();

            left = binaryInfixCall(function, left, right);
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

                Token opToken = lexer.next();
                ExprFunction op = function(opToken);
                ExprNode operand = unary();
                SourceRange sourceRange = new SourceRange(opToken.getStart(), operand.getSourceRange().getEnd());
                return new FunctionCallNode(op, singletonList(operand), sourceRange);
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
                Token opToken = lexer.next();
                ExprFunction op = function(opToken);
                ExprNode operand = primary();
                SourceRange sourceRange = new SourceRange(opToken.getStart(), operand.getSourceRange().getEnd());
                return new FunctionCallNode(op, singletonList(operand), sourceRange);
            }
        }
        return primary();
    }

    private ExprNode primary() {
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
                ExprNode e = expression();
                Token closeToken = expect(TokenType.PAREN_END);
                return new GroupExpr(e, new SourceRange(openToken, closeToken));

            default:
                throw new ExprSyntaxException(new SourceRange(lexer.peek()),
                        "Expected a symbol, a number, a string, or '('");
        }
    }

    private ExprNode stringLiteral() {
        Token token = lexer.next();
        assert token.getType() == TokenType.STRING_LITERAL;
        return new ConstantExpr(token.getString(), new SourceRange(token));
    }

    private ExprNode enumLiteral() {
        Token token = lexer.next();
        assert token.getType() == TokenType.SYMBOL;
        return new ConstantExpr(token, new SourceRange(token));
    }

    private ExprNode booleanLiteral() {
        Token token = lexer.next();
        assert token.getType() == TokenType.BOOLEAN_LITERAL;
        boolean value = token.getString().toLowerCase().equals("true");
        SourceRange source = new SourceRange(token);
        return new ConstantExpr(value, source);
    }

    private ExprFunction function() {
        Token token = lexer.next();
        return function(token);
    }


    private FunctionCallNode binaryInfixCall(ExprFunction op, ExprNode left, ExprNode right) {
        SourceRange source = new SourceRange(left.getSourceRange(), right.getSourceRange());
        return new FunctionCallNode(op, Arrays.asList(left, right), source);
    }


    private ExprFunction function(Token token) {
        try {
            return ExprFunctions.get(token.getString());
        } catch (UnsupportedOperationException e) {
            throw new ExprSyntaxException(new SourceRange(token), "'" + token.getString() + "' is not a function.");
        }
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
                Token closingParen = lexer.next();
                return new FunctionCallNode(function, arguments, new SourceRange(functionToken, closingParen));
            }

            // Otherwise parse the next argument
            arguments.add(expression());
        }
    }

    private ExprNode compound(SymbolExpr symbol) {
        ExprNode result = symbol;

        while(lexer.hasNext() && lexer.peek().isDot()) {
            lexer.next();
            SymbolExpr field = symbol();
            result = new CompoundExpr(result, field, new SourceRange(result.getSourceRange(), field.getSourceRange()));
        }
        return result;
    }

    private SymbolExpr symbol() {
        Token token = lexer.next();
        return symbol(token);
    }

    private SymbolExpr symbol(Token token) {
        assert token.getType() == TokenType.SYMBOL;
        return new SymbolExpr(token);
    }

    private ExprNode number() {
        Token token = lexer.next();
        double value;
        try {
            value = Double.parseDouble(token.getString());
        } catch (NumberFormatException e) {
            throw new ExprSyntaxException(new SourceRange(token), "Invalid number '" + token.getString() + "': " +
                    e.getMessage());
        }
        return new ConstantExpr(value, new SourceRange(token));
    }

    public ExprNode expression() {
        // Start with the operator with the highest precedence
        return disjunction();
    }

    public ExprNode parse() {
        ExprNode expr = expression();
        if(lexer.hasNext()) {
            Token extraToken = lexer.next();
            throw new ExprSyntaxException(new SourceRange(extraToken), "Missing an operator like + - / *, etc.");
        }
        return expr;
    }

    public static ExprNode parse(String expression) {
        if(Strings.isNullOrEmpty(expression)) {
            return new ConstantExpr((String)null);
        }
        ExprLexer lexer = new ExprLexer(expression);
        ExprParser parser = new ExprParser(lexer);
        return parser.parse();
    }
}
