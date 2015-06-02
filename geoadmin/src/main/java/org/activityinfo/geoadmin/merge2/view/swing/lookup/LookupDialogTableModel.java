package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import com.google.common.collect.Lists;
import org.activityinfo.geoadmin.merge2.view.mapping.LookupTable;
import org.activityinfo.geoadmin.merge2.view.mapping.SourceLookupKey;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;

import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Table model displaying choice of target instances to look up.
 */
public class LookupDialogTableModel extends AbstractTableModel {

    private final SourceLookupKey sourceKey;
    private List<FieldProfile> targetKeyFields;
    private List<Integer> targetCandidateRows;
    private double scores[];
    private NumberFormat scoreFormat = new DecimalFormat("0.00");

    public LookupDialogTableModel(LookupTable lookupTable, int sourceKeyIndex) {
        this.targetKeyFields = lookupTable.getTargetKeyFields();
        this.targetCandidateRows = Lists.newArrayList(lookupTable.getTargetCandidateRows(sourceKeyIndex));
        this.sourceKey = lookupTable.getSourceKey(sourceKeyIndex);
        
        scores = new double[targetCandidateRows.size()];
        for (int i = 0; i < targetCandidateRows.size(); i++) {
            scores[i] = lookupTable.getGraph().getScore(sourceKeyIndex, targetCandidateRows.get(i));
        }
    }
    
    public int convertTableIndexToModel(int targetRowIndex) {
        return targetCandidateRows.indexOf(targetRowIndex);
    }

    @Override
    public int getRowCount() {
        return targetCandidateRows.size();
    }

    @Override
    public int getColumnCount() {
        return targetKeyFields.size() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex == 0) {
            return scoreFormat.format(scores[rowIndex]);
        } else {
            int targetRow = targetCandidateRows.get(rowIndex);
            FieldProfile targetField = targetKeyFields.get(columnIndex - 1);
            
            return targetField.getView().getString(targetRow);
        }
    }

    @Override
    public String getColumnName(int column) {
        if(column == 0) {
            return "Score";
        } else {
            return sourceKey.get(column-1);
        }
    }
}
