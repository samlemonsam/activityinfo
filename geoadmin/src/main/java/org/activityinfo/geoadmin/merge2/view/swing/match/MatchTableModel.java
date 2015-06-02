package org.activityinfo.geoadmin.merge2.view.swing.match;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.match.MatchTable;
import org.activityinfo.geoadmin.merge2.view.match.MatchTableColumn;
import org.activityinfo.observable.Subscription;
import org.activityinfo.observable.TableObserver;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class MatchTableModel extends AbstractTableModel {

    private final Subscription subscription;
    private List<MatchTableColumn> columns = new ArrayList<>();

    private int rowCount = 0;

    public MatchTableModel(ImportView view) {
        final MatchTable matchTable = view.getMatchTable();
        subscription = matchTable.subscribe(new TableObserver() {
            @Override
            public void onRowsChanged() {
                if (matchTable.isLoading()) {
                    rowCount = 0;
                } else {
                    rowCount = matchTable.getRowCount();
                }
                fireTableDataChanged();
            }

            @Override
            public void onRowChanged(int index) {

            }
        });
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
        return rowCount;
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
        subscription.unsubscribe();
    }
}
