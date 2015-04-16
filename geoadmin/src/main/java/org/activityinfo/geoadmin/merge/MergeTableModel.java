package org.activityinfo.geoadmin.merge;

import org.activityinfo.model.formTree.FormTree;

import javax.swing.table.AbstractTableModel;
import java.util.List;


public class MergeTableModel extends AbstractTableModel {

    private final FormTree targetTree;
    private final List<FormTree.Node> targetLeaves;
    private FormTree sourceTree;
    
    public MergeTableModel(FormTree targetTree) {
        this.targetTree = targetTree;
        this.targetLeaves = targetTree.getLeaves();
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
