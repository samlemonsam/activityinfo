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

import com.google.common.primitives.UnsignedBytes;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.SortDir;
import org.activityinfo.model.util.HeapsortColumn;

/**
 * Compact ColumnView for numbers are all integers and have a range of less than 255
 */
public class IntColumnView8 extends AbstractNumberColumn {

    static final int MAX_RANGE = 255;

    private byte[] values;
    private int delta;

    public IntColumnView8(double doubleValues[], int numRows, int minValue) {
        this.values = new byte[numRows];

        // Reserve 0 for missing values
        this.delta = minValue - 1;

        for (int i = 0; i < numRows; i++) {
            double doubleValue = doubleValues[i];
            if(!Double.isNaN(doubleValue)) {
                values[i] = (byte)(doubleValue - this.delta);
            }
        }
    }

    private IntColumnView8(byte[] values, int delta) {
        this.values = values;
        this.delta = delta;
    }

    @Override
    public int numRows() {
        return values.length;
    }

    @Override
    public double getDouble(int row) {
        byte byteValue = values[row];
        if(byteValue == 0) {
            return Double.NaN;
        } else {
            return delta + UnsignedBytes.toInt(byteValue);
        }
    }

    @Override
    public boolean isMissing(int row) {
        return values[row] == 0;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        byte[] selectedValues = new byte[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow != -1) {
                selectedValues[i] = this.values[selectedRow];
            }
        }
        return new IntColumnView8(selectedValues, delta);
    }

    @Override
    public int[] order(int[] sortVector, SortDir direction, int[] range) {
        int numRows = values.length;
        if (range == null || range.length == numRows) {
            HeapsortColumn.heapsortByte(values, sortVector, numRows,
                    HeapsortColumn.withDirection(IntColumnView8::isLessThan, direction));
        } else {
            HeapsortColumn.heapsortByte(values, sortVector, range.length, range,
                    HeapsortColumn.withDirection(IntColumnView8::isLessThan, direction));
        }
        return sortVector;
    }

    /**
     * Given two numbers encoded as unsigned bytes from 0x01-0xFF, with missing values encoded as zeroes
     *
     */
    private static boolean isLessThan(byte bx, byte by) {
        // Treat as unsigned
        int x = UnsignedBytes.toInt(bx);
        int y = UnsignedBytes.toInt(by);

        // Missing values encoded as zeroes
        boolean xMissing = (x == 0);
        boolean yMissing = (y == 0);

        if(xMissing && !yMissing) {
            return true;
        }
        return x < y;
    }

}
