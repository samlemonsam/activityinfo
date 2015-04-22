package org.activityinfo.geoadmin.merge;

import org.activityinfo.model.formTree.FormTree;

import javax.swing.table.AbstractTableModel;


public class MergeTableModel extends AbstractTableModel {


    public MergeTableModel(FormTree targetTree) {
    }
    
    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }
}
