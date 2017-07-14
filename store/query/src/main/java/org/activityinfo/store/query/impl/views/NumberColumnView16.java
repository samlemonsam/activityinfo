package org.activityinfo.store.query.impl.views;

import com.google.common.primitives.UnsignedBytes;
import org.activityinfo.model.query.ColumnView;

import java.util.Arrays;

/**
 * Compact ColumnView for numbers are all integers and have a range of less than 255
 */
public class NumberColumnView16 extends AbstractNumberColumn {


    public static final int MAX_RANGE = 65535;

    private short[] values;
    private int delta;

    public NumberColumnView16(double doubleValues[], int numRows, int minValue) {
        this.values = new short[numRows];

        // Reserve 0 for missing values
        this.delta = minValue - 1;

        for (int i = 0; i < numRows; i++) {
            double doubleValue = doubleValues[i];
            if(!Double.isNaN(doubleValue)) {
                values[i] = (short) (doubleValue - this.delta);
            }
        }
    }

    public NumberColumnView16(short[] values, int delta) {
        this.values = values;
        this.delta = delta;
    }

    @Override
    public int numRows() {
        return values.length;
    }

    @Override
    public double getDouble(int row) {
        short shortValue = values[row];
        if(shortValue == 0) {
            return Double.NaN;
        } else {
            return delta + (shortValue & 0xFFFF);
        }
    }

    @Override
    public boolean isMissing(int row) {
        return values[row] == 0;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        short[] selectedValues = new short[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow != -1) {
                selectedValues[i] = this.values[selectedRow];
            }
        }
        return new NumberColumnView16(selectedValues, delta);
    }
}
