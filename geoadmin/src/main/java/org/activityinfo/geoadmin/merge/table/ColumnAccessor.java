package org.activityinfo.geoadmin.merge.table;


import javax.swing.table.TableCellRenderer;

public abstract class ColumnAccessor {
    
    public abstract String getHeader();
    
    public abstract Object getValue(int rowIndex);

    public abstract TableCellRenderer getCellRenderer();
    
    public boolean isResizable() {
        return true;
    }

    public int getWidth() {
        throw new UnsupportedOperationException();
    }
}
