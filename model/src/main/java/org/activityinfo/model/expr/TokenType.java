package org.activityinfo.model.expr;

public enum TokenType {


    /**
     * "("
     */
    PAREN_START,

    /**
     * ")"
     */
    PAREN_END,


    /**
     * """
     */
    STRING_LITERAL,


    /**
     * Operator : +, /, -, &&, || etc
     */
    OPERATOR,


    /**
     * A named value
     */
    SYMBOL,

    
    FUNCTION,

    /**
     * Comma: ','
     */
    COMMA,

    /**
     * Dot '.'
     */
    DOT,

    /**
     * Numeric Literal: 1, 3 ...
     */
    NUMBER,

    /**
     * Boolean literal : true, false
     */
    BOOLEAN_LITERAL,

    /**
     * Whitespace
     */
    WHITESPACE,

    EOF


}
