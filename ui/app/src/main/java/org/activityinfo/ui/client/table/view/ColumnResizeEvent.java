package org.activityinfo.ui.client.table.view;

import com.google.gwt.event.shared.GwtEvent;

public class ColumnResizeEvent extends GwtEvent<ColumnResizeHandler> {

    /**
     * The event type.
     */
    private static Type<ColumnResizeHandler> TYPE;
    private int columnIndex;
    private int columnWidth;

    public ColumnResizeEvent(int columnIndex, int columnWidth) {
        this.columnIndex = columnIndex;
        this.columnWidth = columnWidth;
    }

    /**
     * Ensures the existence of the handler hook and then returns it.
     *
     * @return returns a handler hook
     */
    public static Type<ColumnResizeHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    @Override
    public Type<ColumnResizeHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(ColumnResizeHandler handler) {
        handler.onColumnResized(this);
    }
}
