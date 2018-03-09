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
package org.activityinfo.store.query.server.columns;

import org.activityinfo.model.query.*;
import org.activityinfo.model.query.EnumColumnView;
import org.activityinfo.model.util.HeapsortColumn;

import java.io.Serializable;

/**
 * Compact storage for discrete string vectors with fewer than 128 values.
 */
class DiscreteStringColumnView8 implements EnumColumnView, Serializable {

    static final int MAX_COUNT = Byte.MAX_VALUE;

    private String[] ids;
    private String[] labels;
    private byte[] values;

    public DiscreteStringColumnView8() {
    }

    DiscreteStringColumnView8(String[] labels, byte[] values) {
        assert labels.length <= MAX_COUNT;
        this.labels = labels;
        this.values = values;
    }

    public DiscreteStringColumnView8(String[] ids, String[] labels, byte[] values) {
        this(labels, values);
        this.ids = ids;
    }

    @Override
    public String getId(int row) {
        int idIndex = values[row];
        if(idIndex < 0) {
            return null;
        } else {
            return ids[idIndex];
        }
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
    public String get(int row) {
        return getString(row);
    }

    @Override
    public double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public String getString(int row) {
        int labelIndex = values[row];
        if(labelIndex < 0) {
            return null;
        } else {
            return labels[labelIndex];
        }
    }

    @Override
    public int getBoolean(int row) {
        return NA;
    }

    @Override
    public boolean isMissing(int row) {
        return getString(row) == null;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        byte filteredValues[] = new byte[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow < 0) {
                filteredValues[i] = -1;
            } else {
                filteredValues[i] = values[selectedRow];
            }
        }
        return new DiscreteStringColumnView8(ids, labels, filteredValues);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{numRows=" + numRows() + "}";
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        int numRows = values.length;
        switch(direction) {
            case ASC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortEnumAscending(values, labels, sortVector, numRows);
                } else {
                    HeapsortColumn.heapsortEnumAscending(values, labels, sortVector, range.length, range);
                }
                break;
            case DESC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortEnumDescending(values, labels, sortVector, numRows);
                } else {
                    HeapsortColumn.heapsortEnumDescending(values, labels, sortVector, range.length, range);
                }
                break;
        }
        return sortVector;
    }
}
