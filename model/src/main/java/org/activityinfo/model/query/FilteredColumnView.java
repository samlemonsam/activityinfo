package org.activityinfo.model.query;


public class FilteredColumnView implements ColumnView {
    private ColumnView view;
    private int[] filteredIndices;


    public FilteredColumnView(ColumnView view, int[] filteredIndices) {
        this.view = view;
        this.filteredIndices = filteredIndices;
        assert indicesInBounds();
    }


    private boolean indicesInBounds() {
        for (int i = 0; i < filteredIndices.length; i++) {
            int filteredIndex = filteredIndices[i];
            if(filteredIndex >= view.numRows()) {
                return false;
            }
        }
        return true;
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
        int selectedRow = filteredIndices[row];
        if(selectedRow == -1) {
            return Double.NaN;
        }
        return view.getDouble(selectedRow);
    }

    @Override
    public double getDouble(int row) {
        int selectedRow = filteredIndices[row];
        if(selectedRow == -1) {
            return Double.NaN;
        }
        return view.getDouble(selectedRow);
    }

    @Override
    public String getString(int row) {
        int selectedRow = filteredIndices[row];
        if(selectedRow == -1) {
            return null;
        }
        return view.getString(selectedRow);
    }

    @Override
    public int getBoolean(int row) {
        int selectedRow = filteredIndices[row];
        if(selectedRow == -1) {
            return NA;
        }
        return view.getBoolean(selectedRow);
    }

    @Override
    public boolean isMissing(int row) {
        int selectedRow = filteredIndices[row];
        if(selectedRow == -1) {
            return true;
        }
        return view.isMissing(selectedRow);
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        int filteredFilteredIndices[] = new int[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow == -1) {
                filteredFilteredIndices[i] = -1;
            } else {
                filteredFilteredIndices[i] = filteredIndices[selectedRow];
            }
        }
        return new FilteredColumnView(view, filteredFilteredIndices);
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
