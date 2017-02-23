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
