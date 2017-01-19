package org.activityinfo.store.query.impl.views;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;


public class FilteredColumnView implements ColumnView {
    private ColumnView view;
    private int[] filteredIndices;

    public FilteredColumnView(ColumnView view, int[] filteredIndices) {
        this.view = view;
        this.filteredIndices = filteredIndices;
    }

    @Override
    public ColumnType getType() {
        return view.getType();
    }

    @Override
    public int numRows() {
        return filteredIndices.length;
    }

    @Override
    public Object get(int row) {
        return view.get(filteredIndices[row]);
    }

    @Override
    public double getDouble(int row) {
        return view.getDouble(filteredIndices[row]);
    }

    @Override
    public String getString(int row) {
        return view.getString(filteredIndices[row]);
    }

    @Override
    public int getBoolean(int row) {
        return view.getBoolean(filteredIndices[row]);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (int i = 0; i < numRows(); i++) {
            if(i > 0) {
                s.append(", ");
            }
            s.append(get(i));
        }
        s.append("]");
        return s.toString();
    }
}
