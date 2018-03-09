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

public class SourceRange {
    private final SourcePos start;
    private final SourcePos end;

    public SourceRange(SourcePos start, int length) {
        this.start = start;
        this.end = new SourcePos(start.getLine(), start.getColumn() + length);
    }

    public SourceRange(SourcePos start, SourcePos end) {
        this.start = start;
        this.end = end;
    }

    public SourceRange(Token token) {
        this(token.getStart(), token.getLength());
    }

    public SourceRange(Token startToken, Token endToken) {
        this(startToken.getStart(), endToken.getEnd());
    }

    public SourceRange(SourceRange start, SourceRange end) {
        this(start.getStart(), end.getEnd());
    }

    public SourcePos getStart() {
        return start;
    }

    public SourcePos getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceRange that = (SourceRange) o;

        if (!start.equals(that.start)) return false;
        return end.equals(that.end);

    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SourceRange{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
