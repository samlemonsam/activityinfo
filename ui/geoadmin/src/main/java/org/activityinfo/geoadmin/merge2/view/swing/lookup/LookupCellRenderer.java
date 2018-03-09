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
            if (lookupTable.get().isResolved(sourceKeyIndex)) {
                component.setBackground(Color.GREEN);
            } else {
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
        }
        return component;
    }
}
