package org.activityinfo.geoadmin.merge.table;

import org.activityinfo.geoadmin.merge.model.MatchRow;
import org.activityinfo.geoadmin.merge.model.MergeColumn;
import org.activityinfo.geoadmin.merge.model.MergeModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
* Displays the original target Column
*/
class TargetColumnAccessor extends ColumnAccessor {
    private MergeModel mergeModel;
    private MergeColumn targetColumn;
    
    public TargetColumnAccessor(MergeModel mergeModel, MergeColumn targetColumn) {
        this.mergeModel = mergeModel;
        this.targetColumn = targetColumn;
    }
    
    @Override
    public String getHeader() {
        return targetColumn.getLabel();
    }

    @Override
    public Object getValue(int rowIndex) {
        MatchRow match = mergeModel.getMatch(rowIndex);
        if(match.isTargetMatched()) {
            return targetColumn.getView().get(match.getTarget());

        } else {
            return null;
        }
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                                                           boolean isSelected, 
                                                           boolean hasFocus, 
                                                           int row, 
                                                           int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if(!isSelected) {
                    MatchRow match = mergeModel.getMatch(table.convertRowIndexToModel(row));
                    if (match.isTargetMatched()) {
                        c.setBackground(MergeColors.TARGET_COLOR);
                    } else {
                        c.setBackground(MergeColors.DELETED_COLOR);
                    }
                } 
                return c;
            }
        };
    }

}
