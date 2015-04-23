package org.activityinfo.geoadmin.merge.table;

import org.activityinfo.geoadmin.merge.model.MatchRow;
import org.activityinfo.geoadmin.merge.model.MergeColumn;
import org.activityinfo.geoadmin.merge.model.MergeModel;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
* Created by alex on 22-4-15.
*/
class SourceColumnAccessor extends ColumnAccessor {

    private final MergeModel mergeModel;
    private final MergeColumn sourceColumn;
    private final MergeColumn targetColumn;
    
    private final LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();

    public SourceColumnAccessor(MergeModel mergeModel, MergeColumn sourceColumn, MergeColumn targetColumn) {
        this.mergeModel = mergeModel;
        this.sourceColumn = sourceColumn;
        this.targetColumn = targetColumn;
    }

    @Override
    public String getHeader() {
        return sourceColumn.getLabel();
    }

    @Override
    public Object getValue(int rowIndex) {
        MatchRow match = mergeModel.getMatch(rowIndex);
        if(match.isSourceMatched()) {
            return sourceColumn.getView().get(match.getSource());
        
        } else {
            return null;
        }
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if(!isSelected) {
                    MatchRow match = mergeModel.getMatch(table.convertRowIndexToModel(row));
                    c.setBackground(highlightColor(match));
                }
                return c;
            }
        };
    }
    
    private Color highlightColor(MatchRow match) {
        
        // If this row has no matched source record, than the source represents a potential deletion
        if(!match.isSourceMatched()) {
            return MergeColors.DELETED_COLOR;
        }
        
        // If this row has no matched target record, it is a potential insertion
        if(!match.isTargetMatched()) {
            return MergeColors.SOURCE_COLOR;
        }

        // If this column is not matched to a target column, treat it as reference data
        if(targetColumn == null) {
            return Color.WHITE;
        }
        
        // Does this cell value represent a change?
        if(isChange(match)) {
            return MergeColors.SOURCE_COLOR;
        }
        
        // Otherwise, no change to target
        return MergeColors.TARGET_COLOR;
    }

    private boolean isChange(MatchRow match) {
        if(match.isTargetMatched()) {
            String targetValue = targetColumn.getView().getString(match.getTarget());
            String sourceValue = sourceColumn.getView().getString(match.getSource());
            return targetValue != null && sourceValue != null && scorer.score(targetValue, sourceValue) > 0.95;
        } else {
            return false;
        }
    }
}
