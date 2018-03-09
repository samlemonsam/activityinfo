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

/**
 * Indicates a position within the original formula source
 */
public class SourcePos {
    private int line;
    private int column;

    public SourcePos(int line, int column) {
        this.line = line;
        this.column = column;
    }

    /**
     * @return the line index of this token
     */
    public int getLine() {
        return line;
    }

    /**
     *
     * @return the column index of this token from the beginning of the line.
     */
    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourcePos sourcePos = (SourcePos) o;

        if (line != sourcePos.line) return false;
        return column == sourcePos.column;

    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        return result;
    }

    @Override
    public String toString() {
        return "SourcePos{" + line + ":" + column + "}";
    }
}
