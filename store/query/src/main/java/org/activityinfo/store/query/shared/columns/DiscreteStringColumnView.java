package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EnumColumnView;
import org.activityinfo.model.query.SortModel;
import org.activityinfo.model.util.HeapsortColumn;

import java.io.Serializable;

public class DiscreteStringColumnView implements EnumColumnView, ColumnView, Serializable {

    private String[] ids;
    private String[] labels;
    private int[] values;

    public DiscreteStringColumnView() {
    }

    public DiscreteStringColumnView(String[] labels, int[] values) {
        this.labels = labels;
        this.values = values;
    }

    public DiscreteStringColumnView(String[] ids, String[] labels, int[] values) {
        this(labels, values);
        this.ids = ids;
    }

    @Override
    public String getId(int row) {
        int idIndex = values[row];
        if(idIndex < 0) {
            return null;
        } else {
            return ids[idIndex];
        }
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
        return new DiscreteStringColumnView(ids, labels, filteredValues);
    }

    @Override
    public String toString() {  
        return getClass().getSimpleName() + "{numRows=" + numRows() + "}";
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        int numRows = values.length;
        switch(direction) {
            case ASC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortEnumAscending(values, labels, sortVector, numRows);
                } else {
                    HeapsortColumn.heapsortEnumAscending(values, labels, sortVector, range.length, range);
                }
                break;
            case DESC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortEnumDescending(values, labels, sortVector, numRows);
                } else {
                    HeapsortColumn.heapsortEnumDescending(values, labels, sortVector, range.length, range);
                }
                break;
        }
        return sortVector;
    }
}
