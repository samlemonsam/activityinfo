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

import java.awt.Desktop.Action;
import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import org.activityinfo.geoadmin.model.AdminEntity;

/**
 * A TableModel that combines a list of imported features along with a column
 * where the user can choose the parent in the existing hierarchy.
 * 
 */
public class ImportTableModel extends AbstractTableModel {

    public static final int ACTION_COLUMN = 0;
    public static final int PARENT_COLUMN = 1;

    public static final int NUM_EXTRA_COLUMNS = 2;

    private ImportSource source;
    private AdminEntity[] parents;
    private ImportAction[] action;
    
    public ImportTableModel(ImportSource source) {
        this.source = source;
        this.parents = new AdminEntity[source.getFeatureCount()];
        this.action = new ImportAction[source.getFeatureCount()];
        Arrays.fill(this.action, ImportAction.IMPORT);
    }

    @Override
    public int getColumnCount() {
        return source.getAttributes().size() + NUM_EXTRA_COLUMNS;
    }

    @Override
    public int getRowCount() {
        return source.getFeatures().size();
    }

    public ImportFeature getFeatureAt(int rowIndex) {
        return source.getFeatures().get(rowIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int colIndex) {
        switch (colIndex) {
        case ACTION_COLUMN:
        	return action[rowIndex];
        case PARENT_COLUMN:
            return parents[rowIndex];
        default:
            return source.getFeatures().get(rowIndex).getAttributeValue(colIndex - NUM_EXTRA_COLUMNS);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case ACTION_COLUMN:
        	return ImportAction.class;
        case PARENT_COLUMN:
            return AdminEntity.class;
        default:
            return source.getAttributes().get(columnIndex - NUM_EXTRA_COLUMNS).getType().getBinding();
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case ACTION_COLUMN:
        	return "Action";
        case PARENT_COLUMN:
            return "PARENT";
        default:
            return source.getAttributes().get(columnIndex - NUM_EXTRA_COLUMNS).getName().getLocalPart();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case ACTION_COLUMN:
        case PARENT_COLUMN:
            return true;
        default:
            return false;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == PARENT_COLUMN) {
            parents[rowIndex] = (AdminEntity) aValue;
            fireRowChanged(rowIndex);
        } else if(columnIndex == ACTION_COLUMN) {
        	action[rowIndex] = (ImportAction) aValue;
        	fireRowChanged(rowIndex);
        }
    }

    private void fireRowChanged(int rowIndex) {
        for (int i = 0; i != getColumnCount(); ++i) {
            fireTableCellUpdated(rowIndex, i);
        }
    }

    public AdminEntity getParent(int featureIndex) {
        return parents[featureIndex];
    }

	public ImportAction getActionAt(int featureIndex) {
		return action[featureIndex];
	}
}
