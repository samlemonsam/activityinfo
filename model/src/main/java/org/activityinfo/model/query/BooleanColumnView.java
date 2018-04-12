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

    private int[] values;

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
        if (missingRows(selectedRows)) {
            return new FilteredColumnView(this, selectedRows);
        }

        int[] filteredValues = new int[selectedRows.length];
        for (int i = 0; i < filteredValues.length; i++) {
            int selectedRow = selectedRows[i];
            filteredValues[i] = values[selectedRow];
        }
        return new BooleanColumnView(filteredValues);
    }

    private boolean missingRows(int[] selectedRows) {
        for (int i = 0; i < selectedRows.length; i++) {
            if (selectedRows[i] < 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        int numRows = values.length;
        switch(direction) {
            case ASC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortAscending(values, sortVector, numRows);
                } else {
                    HeapsortColumn.heapsortAscending(values, sortVector, range.length, range);
                }
                break;
            case DESC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortDescending(values, sortVector, numRows);
                } else {
                    HeapsortColumn.heapsortDescending(values, sortVector, range.length, range);
                }
                break;
            default:
                break;
        }
        return sortVector;
    }
}
