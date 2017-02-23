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
