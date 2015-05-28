package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.view.match.MatchTableColumn;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MergeColumnRenderer extends DefaultTableCellRenderer {
    private final MatchTableColumn column;

    public MergeColumnRenderer(MatchTableColumn column) {
        this.column = column;
        setHorizontalAlignment(column.getTextAlignment());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            c.setBackground(this.column.getColor(table.convertRowIndexToModel(row)));
        }
        return c;
    }
}
