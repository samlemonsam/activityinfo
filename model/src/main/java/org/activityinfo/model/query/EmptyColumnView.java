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
package org.activityinfo.model.query;

import java.io.Serializable;

public class EmptyColumnView implements ColumnView, Serializable {

    private ColumnType type;
    private int rowCount;

    protected EmptyColumnView() {
    }

    public EmptyColumnView(ColumnType type, int rowCount) {
        this.type = type;
        this.rowCount = rowCount;
    }

    @Override
    public ColumnType getType() {
        return type;
    }

    @Override
    public int numRows() {
        return rowCount;
    }

    @Override
    public Object get(int row) {
        return null;
    }

    @Override
    public double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return NA;
    }

    @Override
    public boolean isMissing(int row) {
        return true;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        return new EmptyColumnView(this.type, selectedRows.length);
    }

    @Override
    public String toString() {
        return "[ " + numRows() + " empty values ]";
    }

    @Override
    public int[] order(int[] sortVector, SortDir direction, int[] range) {
        return sortVector;
    }
}
