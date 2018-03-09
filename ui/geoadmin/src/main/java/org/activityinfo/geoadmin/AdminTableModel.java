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
