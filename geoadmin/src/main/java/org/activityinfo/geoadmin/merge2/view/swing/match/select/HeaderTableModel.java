package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.match.MatchRow;
import org.activityinfo.geoadmin.merge2.view.match.MatchSide;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;

import javax.swing.table.AbstractTableModel;


class HeaderTableModel extends AbstractTableModel {
    private final KeyFieldPairSet keyFields;
    private final MatchSide side;
    private final int index;

    public HeaderTableModel(KeyFieldPairSet keyFields, MatchRow matchRow, MatchSide side) {
        this.keyFields = keyFields;
        this.side = side;
        this.index = matchRow.getRow(side);
    }

    public int getColumnCount() { return keyFields.size();}

    public int getRowCount() { return 1; }

    public String getColumnName(int col) {
        return keyFields.getField(col, side).getLabel();
    }

    public Object getValueAt(int row, int col) {
        return keyFields.getField(col, side).getView().getString(index);
    }
}
