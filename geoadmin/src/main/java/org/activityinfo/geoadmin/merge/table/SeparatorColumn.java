package org.activityinfo.geoadmin.merge.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Separates the columns
 */
public class SeparatorColumn extends ColumnAccessor {
    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public Object getValue(int rowIndex) {
        return "";
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground( UIManager.getColor ( "Panel.background" ) );
                c.setForeground( UIManager.getColor ( "Panel.background" )  );
                return c;
            }
        };
        return renderer;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public int getWidth() {
        return 15;
    }
}
