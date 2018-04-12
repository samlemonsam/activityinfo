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

public class BitSetWithMissingView implements ColumnView, Serializable {

    private int numRows;
    private BitSet bitSet;
    private BitSet missing;

    protected BitSetWithMissingView() {

    }

    public BitSetWithMissingView(int numRows, BitSet bitSet, BitSet missing) {
        this.numRows = numRows;
        this.bitSet = bitSet;
        this.missing = missing;
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public BitSet getMissing() {
        return missing;
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
        if(missing.get(row)) {
            return null;
        } else {
            return bitSet.get(row);
        }
    }

    @Override
    public double getDouble(int row) {
        if(missing.get(row)) {
            return Double.NaN;
        } else {
            return bitSet.get(row) ? 1d : 0d;
        }
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        if(missing.get(row)) {
            return NA;
        } else if(bitSet.get(row)) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    @Override
    public boolean isMissing(int row) {
        return missing.get(row);
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        if (missingRows(selectedRows)) {
            return new FilteredColumnView(this, selectedRows);
        }

        BitSet filtered = new BitSet();
        BitSet filteredMissing = new BitSet();

        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            filtered.set(i, bitSet.get(selectedRow));
            filteredMissing.set(i, missing.get(selectedRow));
        }

        if(filteredMissing.cardinality() == 0) {
            return new BitSetColumnView(selectedRows.length, filtered);
        } else {
            return new BitSetWithMissingView(selectedRows.length, filtered, filteredMissing);
        }
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
