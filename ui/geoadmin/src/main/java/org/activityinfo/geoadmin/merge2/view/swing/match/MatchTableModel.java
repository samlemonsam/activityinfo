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
package org.activityinfo.geoadmin.merge2.view.swing.match;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.match.MatchTable;
import org.activityinfo.geoadmin.merge2.view.match.MatchTableColumn;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class MatchTableModel extends AbstractTableModel {

    private List<MatchTableColumn> columns = new ArrayList<>();

    private MatchTable matchTable;

    public MatchTableModel(ImportView view) {
        matchTable = view.getMatchTable();
    }

    public void updateColumns(List<MatchTableColumn> columns) {
        this.columns = columns;
        fireTableStructureChanged();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getHeader();
    }

    @Override
    public int getRowCount() {
        if(matchTable.isLoading()) {
            return 0;
        } else {
            return matchTable.getRowCount();
        }
    }
    
    

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex < columns.size()) {
            return columns.get(columnIndex).getValue(rowIndex);
        }
        return null;
    }

    public void stop() {
    }
}
