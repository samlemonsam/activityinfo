package org.activityinfo.geoadmin.merge2.view.swing.match;

import org.activityinfo.geoadmin.merge2.view.match.MatchTableColumn;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MatchColumnRenderer extends DefaultTableCellRenderer {
    private final MatchTableColumn column;

    public MatchColumnRenderer(MatchTableColumn column) {
        this.column = column;
        setHorizontalAlignment(column.getTextAlignment());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            Color highlightColor = this.column.getColor(table.convertRowIndexToModel(row));
            c.setBackground(highlightColor);
            
            if(highlightColor.equals(Color.RED)) {
                c.setForeground(Color.WHITE);
            } else {
                c.setForeground(Color.BLACK);
            }
        }
        return c;
    }
}
