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

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.FilteredColumnView;
import org.activityinfo.model.query.SortModel;
import org.activityinfo.model.util.HeapsortColumn;

public class SparseNumberColumnView extends AbstractNumberColumn {

    private final int numRows;
    private final Int2DoubleOpenHashMap map;

    public SparseNumberColumnView(double[] elements, int numRows, int numMissing) {

        this.numRows = numRows;
        this.map = new Int2DoubleOpenHashMap(numRows - numMissing);
        this.map.defaultReturnValue(Double.NaN);

        for (int i = 0; i < numRows; i++) {
            double value = elements[i];
            if(!Double.isNaN(value)) {
                map.put(i, value);
            }
        }
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public double getDouble(int row) {
        return map.get(row);
    }

    @Override
    public boolean isMissing(int row) {
        return !map.containsKey(row);
    }

    @Override
    public ColumnView select(int[] rows) {
        return new FilteredColumnView(this, rows);
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        if (range == null || range.length == numRows) {
            HeapsortColumn.heapsortSparseDouble(map, sortVector, numRows, direction == SortModel.Dir.ASC);
        } else {
            HeapsortColumn.heapsortSparseDouble(map, sortVector, range.length, range, direction == SortModel.Dir.ASC);
        }
        return sortVector;
    }

}
