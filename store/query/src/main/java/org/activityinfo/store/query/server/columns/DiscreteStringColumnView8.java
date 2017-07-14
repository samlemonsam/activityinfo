package org.activityinfo.store.query.server.columns;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.io.Serializable;

/**
 * Compact storage for discrete string vectors with fewer than 128 values.
 */
class DiscreteStringColumnView8 implements ColumnView, Serializable {

    static final int MAX_COUNT = Byte.MAX_VALUE;

    private String[] labels;
    private byte[] values;

    public DiscreteStringColumnView8() {
    }

    DiscreteStringColumnView8(String[] labels, byte[] values) {
        assert labels.length <= MAX_COUNT;
        this.labels = labels;
        this.values = values;
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
        return new DiscreteStringColumnView8(labels, filteredValues);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{numRows=" + numRows() + "}";
    }
}
