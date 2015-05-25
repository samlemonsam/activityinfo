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
 * Displays a column showing the result of the target and source merges
 */
class MergedColumnAccessor extends ColumnAccessor {
    private MergeModel mergeModel;
    private final MergeColumn targetColumn;
    private final MergeColumn sourceColumn;

    public MergedColumnAccessor(MergeModel mergeModel, MergeColumn targetColumn, MergeColumn sourceColumn) {
        this.mergeModel = mergeModel;
        this.targetColumn = targetColumn;
        this.sourceColumn = sourceColumn;
    }


    @Override
    public String getHeader() {
        return targetColumn.getLabel();
    }

    @Override
    public Object getValue(int rowIndex) {
        MatchRow match = mergeModel.getMatch(rowIndex);
        if(match.isDeleted()) {
            return null;
        } else if(sourceColumn != null && match.isSourceMatched()) {
            return sourceColumn.getView().get(match.getSource());
            
        } else if(match.isTargetMatched()) {
            return targetColumn.getView().get(match.getTarget());


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
                if (!isSelected) {
                    MatchRow match = mergeModel.getMatch(table.convertRowIndexToModel(row));
                    if (match.isDeleted()) {
                        c.setBackground(MergeColors.DELETED_COLOR);
                    } else if (match.isTargetMatched() && !isChange(match)) {
                        c.setBackground(MergeColors.TARGET_COLOR);
                    } else {
                        c.setBackground(MergeColors.SOURCE_COLOR);
                    }
                }
                return c;
            }
        };
    }

    private boolean isChange(MatchRow match) {
        LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
        if(match.isTargetMatched() && match.isSourceMatched() &&
            sourceColumn != null && targetColumn != null) {
            String targetValue = targetColumn.getView().getString(match.getTarget());
            String sourceValue = sourceColumn.getView().getString(match.getSource());
            return targetValue == null || sourceValue == null || scorer.score(targetValue, sourceValue) < 0.95;
        } else {
            return false;
        }
    }
}
