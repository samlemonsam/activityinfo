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
package org.activityinfo.api.client;

import com.google.gwt.core.client.JavaScriptObject;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EnumColumnView;
import org.activityinfo.model.query.SortDir;


@SuppressWarnings("GwtInconsistentSerializableClass")
class ColumnArrayView implements EnumColumnView, ColumnView {
   
    private ColumnType columnType;
    private JavaScriptObject array;

    ColumnArrayView(ColumnType columnType, JavaScriptObject array) {
        this.columnType = columnType;
        this.array = array;
    }

    @Override
    public String getId(int row) {
        return getId(array, row);
    }

    @Override
    public ColumnType getType() {
        return columnType;
    }

    @Override
    public int numRows() {
        return getLength(array);
    }

    @Override
    public Object get(int row) {
        switch (columnType) {
            case STRING:
                return getString(row);
            case NUMBER:
                return getDouble(row);
            case BOOLEAN:
                return getBoolean(row);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(int row) {
        return getDouble(array, row);
    }

    @Override
    public String getString(int row) {
        return getString(array, row);
    }

    @Override
    public int getBoolean(int row) {
        return getBoolean(array, row);
    }

    @Override
    public boolean isMissing(int row) {
        switch (columnType) {
            case STRING:
                return getString(row) == null;
            case NUMBER:
                return Double.isNaN(getDouble(row));
            case BOOLEAN:
                return getBoolean(row) == NA;
        }
        return true;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        throw new UnsupportedOperationException();
    }

    private static native int getLength(JavaScriptObject array) /*-{
        return array.length;
    }-*/;
    
    private static native String getString(JavaScriptObject array, int index) /*-{
        return array[index];
    }-*/;

    private static native double getDouble(JavaScriptObject array, int index) /*-{
        var value = array[index];
        return value == null ? NaN : +value;
    }-*/;
    
    private static native int getBoolean(JavaScriptObject array, int index) /*-{
        var value = array[index];
        return value == null ? @org.activityinfo.model.query.ColumnView::NA : (value ? 1 : 0);
    }-*/;

    private static native String getId(JavaScriptObject array, int index) /*-{
        return array[index];
    }-*/;

    @Override
    public int[] order(int[] sortVector, SortDir direction, int[] range) {
        // TODO: Enum Sorting
        // Do not sort on column
        return sortVector;
    }

}
