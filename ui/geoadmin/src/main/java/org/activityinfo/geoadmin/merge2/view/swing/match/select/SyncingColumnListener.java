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
package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

/**
* Keeps a header table's column sizes in sync with a body table below.
*/
class SyncingColumnListener implements TableColumnModelListener {

    private JTable headerTable;
    private JTable bodyTable;

    public SyncingColumnListener(JTable headerTable, JTable bodyTable) {
        this.headerTable = headerTable;
        this.bodyTable = bodyTable;
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {

    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {

    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {

    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        for (int i = 0; i < headerTable.getColumnCount(); i++) {
            TableColumn masterColumn = headerTable.getColumnModel().getColumn(i);
            int headerWidth = masterColumn.getWidth();
            
            TableColumn bodyColumn = bodyTable.getColumnModel().getColumn(i);
            bodyColumn.setPreferredWidth(headerWidth);
        }
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {

    }
}
