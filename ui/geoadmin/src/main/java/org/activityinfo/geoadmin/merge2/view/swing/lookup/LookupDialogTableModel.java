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
package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import com.google.common.collect.Lists;
import org.activityinfo.geoadmin.merge2.view.mapping.LookupTable;
import org.activityinfo.geoadmin.merge2.view.mapping.SourceLookupKey;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.resource.ResourceId;

import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Table model displaying choice of target instances to look up.
 */
public class LookupDialogTableModel extends AbstractTableModel {

    public static final int SCORE_COLUMN = 0;
    
    private final SourceLookupKey sourceKey;
    private List<FieldProfile> targetKeyFields;
    private List<Integer> targetCandidateRows;
    private double scores[];
    private NumberFormat scoreFormat = new DecimalFormat("0.00");
    private LookupTable lookupTable;

    public LookupDialogTableModel(LookupTable lookupTable, int sourceKeyIndex) {
        this.lookupTable = lookupTable;
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
        if(columnIndex == SCORE_COLUMN) {
            return scoreFormat.format(scores[rowIndex]);
        } else {
            int targetRow = targetCandidateRows.get(rowIndex);
            FieldProfile targetField = targetKeyFields.get(columnIndex - 1);
            
            return targetField.getView().getString(targetRow);
        }
    }
    
    public ResourceId getTargetInstanceId(int candidateIndex) {
        Integer targetIndex = targetCandidateRows.get(candidateIndex);
        return lookupTable.getTargetForm().getRowId(targetIndex);
    }

    @Override
    public String getColumnName(int column) {
        if(column == SCORE_COLUMN) {
            return "Score";
        } else {
            return sourceKey.get(column-1);
        }
    }
}
