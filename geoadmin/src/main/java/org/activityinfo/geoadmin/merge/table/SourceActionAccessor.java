package org.activityinfo.geoadmin.merge.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
* Created by alex on 22-4-15.
*/
class SourceActionAccessor extends ColumnAccessor {

    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public Object getValue(int rowIndex) {
        return "«";
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }
}
