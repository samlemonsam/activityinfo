package org.activityinfo.model.expr;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.expr.functions.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExprParser {

    private static final Set<String> INFIX_OPERATORS = Sets.newHashSet("+", "-", "*", "/", "&&", "||", "==", "!=" );

    private static final Set<String> PREFIX_OPERATORS = Sets.newHashSet("!" );

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

    public ExprNode parse() {
        ExprNode expr = parseSimple();
        if (!lexer.hasNext()) {
            return expr;
        }
        Token token = lexer.peek();
        if (isInfixOperator(token)) {
            lexer.next();
            ExprFunction function = ExprFunctions.get(token.getString());
            ExprNode right = parse();

            return new FunctionCallNode(function, expr, right);

        } else if(token.getType() == TokenType.DOT) {
            lexer.next();
            return new CompoundExpr(expr, new SymbolExpr(expectNext(TokenType.SYMBOL, "Field name").getString()));
        } else {
            return expr;
        }
    }

    private boolean isInfixOperator(Token token) {
        return token.getType() == TokenType.OPERATOR &&
                INFIX_OPERATORS.contains(token.getString());
    }

    public ExprNode parseSimple() {
        Token token = lexer.next();
        if (token.getType() == TokenType.PAREN_START) {
            return parseGroup();

        } else if (token.getType() == TokenType.SYMBOL) {
            if(isFunction(token)) {
                return parseFunctionCall(token);
            } else {
                return new SymbolExpr(token.getString());
            }

        } else if (token.getType() == TokenType.NUMBER) {
            return new ConstantExpr(Double.parseDouble(token.getString()));

        } else if (token.getType() == TokenType.BOOLEAN_LITERAL) {
            return new ConstantExpr(Boolean.parseBoolean(token.getString()));

        } else if (prefixOperator(token)) {
            ExprFunction function = ExprFunctions.get(token.getString());
            ExprNode right = parse();
            return new FunctionCallNode(function, right);

        } else if (token.getType() == TokenType.STRING_LITERAL) {
            return new ConstantExpr(token.getString());

        } else if (token.getType() == TokenType.SYMBOL) {
            return new SymbolExpr(token.getString());

        } else {
            throw new ExprSyntaxException("Unexpected token '" + token.getString() + "' at position " + token.getTokenStart() + "'");
        }
    }

    private ExprNode parseFunctionCall(Token token) {
        ExprFunction function = ExprFunctions.get(token.getString());
        List<ExprNode> arguments = Lists.newArrayList();
        while(lexer.hasNext()) {
            Token next = lexer.next();

            if (next.getType() == TokenType.COMMA || next.getType() == TokenType.PAREN_START) {
                continue;

            } else if (next.getType() == TokenType.PAREN_END) {
                break;

            } else if (next.getType() == TokenType.SYMBOL) {
                arguments.add(new SymbolExpr(next.getString()));

            } else {
                throw new ExprSyntaxException("Unexpected token '" + token.getString() + "' at position " + token.getTokenStart() + "'");
            }
        }
        return new FunctionCallNode(function, arguments);
    }

    private boolean prefixOperator(Token token) {
        return token.getType() == TokenType.OPERATOR && PREFIX_OPERATORS.contains(token.getString());
    }

    private boolean isFunction(Token token) {
        return token.getType() == TokenType.SYMBOL && FUNCTIONS.contains(token.getString()) && lexer.peek().getType() == TokenType.PAREN_START;
    }

    private ExprNode parseGroup() {
        ExprNode expr = parse();
        expectNext(TokenType.PAREN_END, "')'");
        return new GroupExpr(expr);
    }

    /**
     * Retrieves the next token, and throws an exception if it does not match
     * the expected type.
     */
    private Token expectNext(TokenType expectedType, String description) {
        if(!lexer.hasNext()) {
            throw new ExprSyntaxException("Syntax error: expected " + description + " but found end of input.");

        }
        Token token = lexer.next();
        if (token.getType() != expectedType) {
            throw new ExprSyntaxException("Syntax error at " + token.getTokenStart() + ": expected " + description + " but found '" + token.getString() + "'");
        }
        return token;
    }

    public static ExprNode parse(String expression) {
        try {
            ExprParser parser = new ExprParser(new ExprLexer(expression));
            return parser.parse();
        } catch(Exception e) {
            throw new RuntimeException("Failed to parse expression: " + expression, e);
        }
    }
}
