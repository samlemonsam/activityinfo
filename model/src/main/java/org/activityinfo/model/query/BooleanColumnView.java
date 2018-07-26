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

import org.activityinfo.model.util.HeapsortColumn;

/**
 * Boolean column view backed by a int[] array
 */
public class BooleanColumnView implements ColumnView {

    private int values[];

    public BooleanColumnView() {
    }

    public BooleanColumnView(int[] values) {
        this.values = values;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.BOOLEAN;
    }

    @Override
    public int numRows() {
        return values.length;
    }

    @Override
    public Object get(int row) {
        int v = values[row];
        if(v == ColumnView.NA) {
            return null;
        } else {
            return v != 0;
        }
    }

    @Override
    public double getDouble(int row) {
        int v = values[row];
        if(v == ColumnView.NA) {
            return Double.NaN;
        } else {
            return v;
        }
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return values[row];
    }

    @Override
    public boolean isMissing(int row) {
        return values[row] == NA;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        int filteredValues[] = new int[selectedRows.length];
        for (int i = 0; i < filteredValues.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow == -1) {
                filteredValues[i] = NA;
            } else {
                filteredValues[i] = values[selectedRow];
            }

        }
        return new BooleanColumnView(filteredValues);
    }

    @Override
    public int[] order(int[] sortVector, SortDir direction, int[] range) {

        int numRows = values.length;
        if (range == null || range.length == numRows) {
            HeapsortColumn.heapsortInt(values, sortVector, numRows,
                    HeapsortColumn.withIntDirection(BooleanColumnView::isLessThan, direction));
        } else {

            HeapsortColumn.heapsortInt(values, sortVector, range.length, range,
                    HeapsortColumn.withIntDirection(BooleanColumnView::isLessThan, direction));
        }
        return sortVector;
    }


    private static boolean isLessThan(int a, int b) {
        if (a == ColumnView.TRUE) {
            return false;
        } else if (a == ColumnView.FALSE) {
            return b == ColumnView.TRUE;
        } else {
            // a == ColumnView.NA
            return b != ColumnView.NA;
        }
    }


}
