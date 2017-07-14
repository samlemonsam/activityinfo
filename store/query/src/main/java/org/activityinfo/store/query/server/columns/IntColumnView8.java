package org.activityinfo.store.query.server.columns;

import com.google.common.primitives.UnsignedBytes;
import org.activityinfo.model.query.ColumnView;

/**
 * Compact ColumnView for numbers are all integers and have a range of less than 255
 */
class IntColumnView8 extends AbstractNumberColumn {


    static final int MAX_RANGE = 255;

    private byte[] values;
    private int delta;

    IntColumnView8(double doubleValues[], int numRows, int minValue) {
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
}
