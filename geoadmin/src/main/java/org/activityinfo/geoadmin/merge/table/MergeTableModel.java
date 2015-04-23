package org.activityinfo.geoadmin.merge.table;

import org.activityinfo.geoadmin.merge.model.MergeColumn;
import org.activityinfo.geoadmin.merge.model.MergeModel;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class MergeTableModel extends AbstractTableModel {


    private MergeModel mergeModel;
    private List<ColumnAccessor> columns = new ArrayList<>();

    public MergeTableModel(MergeModel mergeModel) {
        this.mergeModel = mergeModel;

        // On the left hand side, show the existing "Target" features
        for (MergeColumn targetColumn : mergeModel.getTargetForm().getTextFields()) {
            columns.add(new TargetColumnAccessor(mergeModel, targetColumn));
        }
        
        columns.add(new SeparatorColumn());

        // In the middle, show the result of the merge
        for (MergeColumn targetColumn : mergeModel.getTargetForm().getTextFields()) {
            columns.add(new MergedColumnAccessor(mergeModel, targetColumn, mergeModel.getSourceColumnForTarget(targetColumn)));
        }

        columns.add(new SeparatorColumn());

        // On the right, show the imported features, in the same order
        // as the columns to which they're mapped
        for (MergeColumn targetColumn : mergeModel.getTargetForm().getTextFields()) {
            MergeColumn sourceColumn = mergeModel.getSourceColumnForTarget(targetColumn);
            if(sourceColumn != null) {
                columns.add(new SourceColumnAccessor(mergeModel, sourceColumn, targetColumn));
            }
        }
        // Also include the unmatched source columns as reference
        for (MergeColumn sourceColumn : mergeModel.getSourceForm().getTextFields()) {
            if(!mergeModel.isMapped(sourceColumn)) {
                columns.add(new SourceColumnAccessor(mergeModel, sourceColumn, null));
            }
        }
    }
    
    @Override
    public int getRowCount() {
        return mergeModel.getMatches().size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return columns.get(columnIndex).getValue(rowIndex);
    }


    @Override
    public String getColumnName(int column) {
        return columns.get(column).getHeader();
    }

    public ColumnAccessor getColumnAccessor(int column) {
        return columns.get(column);
    }

}
