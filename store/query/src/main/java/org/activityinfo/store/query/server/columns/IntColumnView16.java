package org.activityinfo.store.query.server.columns;

import org.activityinfo.model.query.ColumnView;

/**
 * Compact ColumnView for numbers are all integers and have a range of less than 255
 */
class IntColumnView16 extends AbstractNumberColumn {


    static final int MAX_RANGE = 65535;

    private short[] values;
    private int delta;

    IntColumnView16(double doubleValues[], int numRows, int minValue) {
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

    private IntColumnView16(short[] values, int delta) {
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
            // Extract the unsigned value
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
        return new IntColumnView16(selectedValues, delta);
    }
}
