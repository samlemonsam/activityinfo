package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import org.activityinfo.geoadmin.merge2.view.mapping.LookupTable;
import org.activityinfo.geoadmin.merge2.view.match.MatchedColumn;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Color codes lookup keys according to their confidence level
 */
public class LookupCellRenderer extends DefaultTableCellRenderer {
    
    private final LookupTable lookupTable;

    public LookupCellRenderer(LookupTable lookupTable) {
        this.lookupTable = lookupTable;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(!isSelected) {
            int sourceKeyIndex = table.convertRowIndexToModel(row);
            switch (lookupTable.getLookupConfidence(sourceKeyIndex)) {
                case EXACT:
                    component.setBackground(Color.GREEN);
                    break;
                case WARNING:
                    component.setBackground(MatchedColumn.WARNING_COLOR);
                    break;
                case POOR:
                    component.setBackground(Color.RED);
                    break;
            }
        }
        return component;
    }
}
