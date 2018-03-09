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
package org.activityinfo.geoadmin;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.activityinfo.geoadmin.model.AdminEntity;

/**
 * A TableCellRenderer for the import view which colors rows according to the
 * quality of their match to an existing parent.
 */
public class ImportTableCellRenderer extends DefaultTableCellRenderer {

    private static final Color FOREST_GREEN = Color.decode("#4AA02C");
    private static final Color FIREBRICK3 = Color.decode("#C11B17");
    private static final Color PINK = Color.decode("#F660AB");

    private ParentGuesser scorer;
    private ImportTableModel tableModel;

    public ImportTableCellRenderer(ImportTableModel tableModel, ParentGuesser scorer) {
        super();
        this.tableModel = tableModel;
        this.scorer = scorer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {

        final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            int featureIndex = table.convertRowIndexToModel(row);
            if (tableModel.getParent(row) != null) {
                AdminEntity parent = tableModel.getParent(featureIndex);
                ImportFeature feature = tableModel.getFeatureAt(featureIndex);
                switch (scorer.quality(feature, parent)) {
                case OK:
                    c.setBackground(FOREST_GREEN);
                    break;
                case WARNING:
                    c.setBackground(PINK);
                    break;
                case SEVERE:
                    c.setBackground(FIREBRICK3);
                    c.setForeground(Color.WHITE);
                    break;
                }
            }
        }
        return c;
    }
}
