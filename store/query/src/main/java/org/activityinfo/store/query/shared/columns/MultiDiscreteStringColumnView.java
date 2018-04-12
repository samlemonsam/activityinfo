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
package org.activityinfo.store.query.shared.columns;

import com.google.common.base.Strings;
import org.activityinfo.model.query.*;

import java.io.Serializable;
import java.util.BitSet;

public class MultiDiscreteStringColumnView implements EnumColumnView, ColumnView, Serializable {

    public static final char SEPARATOR = ',';

    private String[] ids;
    private String[] labels;
    private BitSet[] selections;
    private int numRows;

    public MultiDiscreteStringColumnView() {
    }

    public MultiDiscreteStringColumnView(int numRows, String[] labels, BitSet[] selections) {
        this.numRows = numRows;
        this.labels = labels;
        this.selections = selections;
    }

    public MultiDiscreteStringColumnView(int numRows, String[] ids, String[] labels, BitSet[] selections) {
        this(numRows, labels, selections);
        this.ids = ids;
    }

    @Override
    public String getId(int row) {
        return concatenateValues(ids, row);
    }

    @Override
    public ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public int numRows() {
        return numRows;
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
        return concatenateValues(labels, row);
    }

    private String concatenateValues(String[] array, int row) {
        StringBuilder value = new StringBuilder();
        for(int i=0; i<array.length; i++) {
            if (selections[i].get(row)) {
                add(value, array[i]);
            }
        }
        return Strings.emptyToNull(value.toString());
    }

    private StringBuilder add(StringBuilder array, String value) {
        if (array.length() > 0) {
            array.append(SEPARATOR);
        }
        return array.append(value);
    }

    @Override
    public int getBoolean(int row) {
        return NA;
    }

    @Override
    public boolean isMissing(int row) {
        return row > (numRows-1);
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        if (missingRows(selectedRows)) {
            return new FilteredColumnView(this, selectedRows);
        }

        BitSet[] filtered = new BitSet[labels.length];
        for (int i=0; i < filtered.length; i++) {
            filtered[i] = new BitSet();
        }

        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            for (int j=0; j < filtered.length; j++) {
                filtered[j].set(i, selections[j].get(selectedRow));
            }
        }

        return new MultiDiscreteStringColumnView(selectedRows.length, ids, labels, filtered);
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
    public String toString() {  
        return getClass().getSimpleName() + "{numRows=" + numRows() + "}";
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        return sortVector;
    }
}
