package org.activityinfo.geoadmin.locations;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.activityinfo.geoadmin.util.GenericTableModel;

public class LocationImportTableCellRenderer extends DefaultTableCellRenderer {

	private final GenericTableModel<LocationFeature> tableModel;
	
	public LocationImportTableCellRenderer(GenericTableModel<LocationFeature> tableModel) {
		this.tableModel = tableModel;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
	
        final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            int modelIndex = table.convertRowIndexToModel(row);
            LocationFeature locationFeature = tableModel.getRowAt(modelIndex);     
        }
        return c;
	}

}
