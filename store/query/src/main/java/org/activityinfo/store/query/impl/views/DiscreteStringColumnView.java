package org.activityinfo.store.query.impl.views;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.io.Serializable;

public class DiscreteStringColumnView implements ColumnView, Serializable {

    private String[] labels;
    private int[] values;

    public DiscreteStringColumnView() {
    }

    public DiscreteStringColumnView(String[] labels, int[] values) {
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
        int filteredValues[] = new int[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow < 0) {
                filteredValues[i] = -1;
            } else {
                filteredValues[i] = values[selectedRow];
            }
        }
        return new DiscreteStringColumnView(labels, filteredValues);
    }

    @Override
    public String toString() {  
        return ColumnViewToString.toString(this);
    }
}
