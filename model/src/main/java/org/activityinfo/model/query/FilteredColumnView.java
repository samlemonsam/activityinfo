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

    @Override
    public int[] order(int[] sortVector, SortDir direction, int[] range) {
        // TODO: FilteredColumnViews - order before filter? Currently sorts on all values
        return view.order(sortVector, direction, range);
    }
}
