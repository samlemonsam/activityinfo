package org.activityinfo.geoadmin.merge.table;

import org.activityinfo.geoadmin.merge.model.MatchRow;
import org.activityinfo.geoadmin.merge.model.MergeColumn;
import org.activityinfo.geoadmin.merge.model.MergeModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by alex on 22-4-15.
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
        
        } else if(match.isTargetMatched()) {
            return targetColumn.getView().get(match.getTarget());

        } else if(match.isSourceMatched()) {
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
                if (!isSelected) {
                    MatchRow match = mergeModel.getMatch(table.convertRowIndexToModel(row));
                    if (match.isDeleted()) {
                        c.setBackground(MergeColors.DELETED_COLOR);
                    } else if (match.isTargetMatched()) {
                        c.setBackground(MergeColors.TARGET_COLOR);
                    } else {
                        c.setBackground(MergeColors.SOURCE_COLOR);
                    }
                }
                return c;
            }
        };
    }
}
