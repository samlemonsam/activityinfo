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
import java.util.BitSet;

/**
 * A {@code ColumnView} of a boolean collection field that uses
 * a {@link java.util.BitSet} for storage. The field must have no
 * missing values.
 */
public class BitSetColumnView implements ColumnView, Serializable {

    private int numRows;
    private BitSet bitSet;

    protected BitSetColumnView() {}

    public BitSetColumnView(int numRows, BitSet bitSet) {
        this.numRows = numRows;
        this.bitSet = bitSet;
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.BOOLEAN;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public Object get(int row) {
        return bitSet.get(row);
    }

    @Override
    public double getDouble(int row) {
        return bitSet.get(row) ? 1d : 0d;
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return bitSet.get(row) ? TRUE : FALSE;
    }

    @Override
    public boolean isMissing(int row) {
        return false;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        if (missingRows(selectedRows)) {
            return new FilteredColumnView(this, selectedRows);
        }

        BitSet filtered = new BitSet();
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            filtered.set(i, bitSet.get(selectedRow));
        }

        return new BitSetColumnView(selectedRows.length, filtered);
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
        // TODO: BitSet Sorting
        // Do not sort on column
        return sortVector;
    }

}
