package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import org.activityinfo.geoadmin.merge2.view.mapping.LookupGraph;
import org.activityinfo.geoadmin.merge2.view.mapping.LookupTable;
import org.activityinfo.geoadmin.merge2.view.match.MatchedColumn;
import org.activityinfo.observable.Observable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Color codes lookup keys according to their confidence level
 */
public class LookupCellRenderer extends DefaultTableCellRenderer {
    
    private final LookupGraph graph;
    private final Observable<LookupTable> lookupTable;

    public LookupCellRenderer(LookupGraph graph, Observable<LookupTable> table) {
        this.graph = graph;
        this.lookupTable = table;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(!isSelected && !lookupTable.isLoading()) {
            int sourceKeyIndex = table.convertRowIndexToModel(row);
            int targetIndex = lookupTable.get().getTargetMatchRow(sourceKeyIndex); 
            switch (graph.getLookupConfidence(sourceKeyIndex, targetIndex)) {
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
