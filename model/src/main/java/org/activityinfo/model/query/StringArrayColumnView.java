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

import java.io.Serializable;
import java.util.List;

/**
 * Simple Array of String values
 */
public class StringArrayColumnView implements ColumnView, Serializable {

    private String[] values;

    protected StringArrayColumnView() {
    }

    public StringArrayColumnView(String[] values) {
        this.values = values;
    }

    public StringArrayColumnView(List<String> values) {
        this.values = values.toArray(new String[values.size()]);
    }

    @Override
    public ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public int numRows() {
        return values.length;
    }

    @Override
    public Object get(int row) {
        return values[row];
    }

    @Override
    public double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public String getString(int row) {
        return values[row];
    }

    @Override
    public int getBoolean(int row) {
        return NA;
    }

    @Override
    public boolean isMissing(int row) {
        return values[row] == null;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        String[] filteredValues = new String[selectedRows.length];
        for (int i = 0; i < filteredValues.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow != -1) {
                filteredValues[i] = this.values[selectedRow];
            }
        }
        return new StringArrayColumnView(filteredValues);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(values.length, 5); i++) {
            if(i > 0) {
                sb.append(", ");
            }
            if(values[i] == null) {
                sb.append("NULL");
            } else {
                sb.append("'");
                sb.append(values[i]);
                sb.append("'");
            }
        }
        if(values.length > 5) {
            sb.append("... length=").append(values.length);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        int numRows = values.length;
        switch(direction) {
            case ASC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortString(values, sortVector, numRows, true);
                } else {
                    HeapsortColumn.heapsortString(values, sortVector, range.length, range, true);
                }
                break;
            case DESC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortString(values, sortVector, numRows, false);
                } else {
                    HeapsortColumn.heapsortString(values, sortVector, range.length, range, false);
                }
                break;
        }
        return sortVector;
    }
}
