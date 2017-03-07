package org.activityinfo.api.client;

import com.google.gwt.core.client.JavaScriptObject;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;


@SuppressWarnings("GwtInconsistentSerializableClass")
class ColumnArrayView implements ColumnView {
   
    private ColumnType columnType;
    private JavaScriptObject array;

    ColumnArrayView(ColumnType columnType, JavaScriptObject array) {
        this.columnType = columnType;
        this.array = array;
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

    private static native int getLength(JavaScriptObject array) /*-{
        return array.length();
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


}
