package org.activityinfo.geoadmin.merge2.view.swing.match;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.match.MatchTable;
import org.activityinfo.geoadmin.merge2.view.match.MatchTableColumn;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class MatchTableModel extends AbstractTableModel {

    private List<MatchTableColumn> columns = new ArrayList<>();

    private MatchTable matchTable;

    public MatchTableModel(ImportView view) {
        matchTable = view.getMatchTable();
    }

    public void updateColumns(List<MatchTableColumn> columns) {
        this.columns = columns;
        fireTableStructureChanged();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getHeader();
    }

    @Override
    public int getRowCount() {
        if(matchTable.isLoading()) {
            return 0;
        } else {
            return matchTable.getRowCount();
        }
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex < columns.size()) {
            return columns.get(columnIndex).getValue(rowIndex);
        }
        return null;
    }

    public void stop() {
    }
}
