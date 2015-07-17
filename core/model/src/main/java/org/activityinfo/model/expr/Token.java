package org.activityinfo.model.expr;

import javax.annotation.Nonnull;


public class Token {

    private TokenType type;
    private String string;
    private int tokenStart;

    public Token(TokenType type, int tokenStart, @Nonnull String string) {
        super();
        assert string != null && string.length() > 0;
        this.type = type;
        this.string = string;
    }

    public Token(TokenType type, int tokenStart, char c) {
        this(type, tokenStart, Character.toString(c));
    }

    /**
     * @return this token's type
     */
    public TokenType getType() {
        return type;
    }

    /**
     * @return the character index within the original expression string
     * in which this token starts
     */
    public int getTokenStart() {
        return tokenStart;
    }

    /**
     * @return the string content of the token
     */
    public String getString() {
        return string;
    }

    public String toString() {
        return type.name() + "[" + string + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((string == null) ? 0 : string.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        Token other = (Token) obj;
        return other.string.equals(string);
    }
}
