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

import com.google.common.annotations.VisibleForTesting;

import javax.annotation.Nonnull;


public class Token {

    private TokenType type;
    private String string;
    private SourcePos pos;
    private int length;

    public Token(TokenType type, SourcePos pos, int length, @Nonnull String string) {
        super();
        this.pos = pos;
        this.length = length;
        assert string != null && string.length() > 0;
        this.type = type;
        this.string = string;
    }

    @VisibleForTesting
    Token(TokenType type, int tokenStart, String string) {
        this(type, new SourcePos(0, tokenStart), string.length(), string);
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
    public int getTokenStartColumn() {
        return pos.getColumn();
    }

    public SourcePos getStart() {
        return pos;
    }


    public SourcePos getEnd() {
        return new SourcePos(pos.getLine(), pos.getColumn() + length);
    }

    /**
     *
     * @return the length of the original token in the source, including any escaping.
     */
    public int getLength() {
        return length;
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

    public boolean isRelationalOperator() {
        return type == TokenType.OPERATOR &&
                (string.equals("<") ||
                 string.equals("<=") ||
                 string.equals(">") ||
                 string.equals(">="));
    }

    public boolean isAdditiveOperator() {
        return type == TokenType.OPERATOR &&
                (string.equals("+") || string.equals("-"));
    }

    public boolean isMultiplicativeOperator() {
        return type == TokenType.OPERATOR &&
                (string.equals("*") || string.equals("/"));
    }

    public boolean isEqualityOperator() {
        return type == TokenType.OPERATOR &&
                (string.equals("==") ||
                 string.equals("!="));
    }


    public boolean isOrOperator() {
        return type == TokenType.OPERATOR && string.equals("||");
    }

    public boolean isAndOperator() {
        return type == TokenType.OPERATOR && string.equals("&&");
    }


    public boolean isDot() {
        return type == TokenType.DOT && string.equals(".");
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
