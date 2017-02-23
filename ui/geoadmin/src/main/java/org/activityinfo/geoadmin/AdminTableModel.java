package org.activityinfo.geoadmin;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.activityinfo.geoadmin.model.AdminEntity;

/**
 * The TableModel for a simple list of existing Administrative entities.
 * 
 */
public class AdminTableModel extends AbstractTableModel {

    private List<AdminEntity> units;

    private static final String[] COLUMNS = new String[] { "ID", "Code", "Name" };
    private static final Class[] COLUMN_TYPE = new Class[] { Integer.class, String.class, String.class };

    public AdminTableModel(List<AdminEntity> units) {
        super();
        this.units = units;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_TYPE[columnIndex];
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMNS[columnIndex];
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public int getRowCount() {
        return units.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int colIndex) {
        switch (colIndex) {
        case 0:
            return units.get(rowIndex).getId();
        case 1:
            return units.get(rowIndex).getCode();
        case 2:
            return units.get(rowIndex).getName();
        }
        throw new UnsupportedOperationException();
    }
}
