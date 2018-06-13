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

public class DoubleArrayColumnView implements ColumnView, Serializable {
    private double[] values;
    private int numRows;

    protected DoubleArrayColumnView() {
    }

    public DoubleArrayColumnView(double[] values) {
        this(values, values.length);
    }

    public DoubleArrayColumnView(double[] values, int numRows) {
        this.values = values;
        this.numRows = numRows;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.NUMBER;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public Object get(int row) {
        double value = values[row];
        if(Double.isNaN(value)) {
            return null;
        } else {
            return value;
        }
    }

    @Override
    public double getDouble(int row) {
        return values[row];
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        double x = getDouble(row);
        if(Double.isNaN(x)) {
            return NA;
        } else if(x == 0) {
            return FALSE;
        } else {
            return TRUE;
        }
    }

    @Override
    public boolean isMissing(int row) {
        return Double.isNaN(values[row]);
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        double filteredValues[] = new double[selectedRows.length];
        for (int i = 0; i < filteredValues.length; i++) {

            int selectedRow = selectedRows[i];
            if(selectedRow == -1) {
                filteredValues[i] = Double.NaN;
            } else {
                filteredValues[i] = values[selectedRow];
            }
        }
        return new DoubleArrayColumnView(filteredValues);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0;i!=Math.min(10, values.length);++i) {
            if(i > 0) {
                sb.append(", ");
            }
            if(Double.isNaN(values[i])) {
                sb.append("NaN");
            } else {
                sb.append(values[i]);
            }
        }
        if(numRows() > 10) {
            sb.append("... total rows = ").append(numRows());
        }
        sb.append(" ]");
        return sb.toString();
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        if (range == null || range.length == numRows) {
            HeapsortColumn.heapsortDouble(values, sortVector, numRows, direction == SortModel.Dir.ASC);
        } else {
            HeapsortColumn.heapsortDouble(values, sortVector, range.length, range, direction == SortModel.Dir.ASC);
        }
        return sortVector;
    }

}
