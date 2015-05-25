package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.MergeModelStore;
import org.activityinfo.geoadmin.merge2.view.model.RowMatching;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.observable.Subscription;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class MergeTableModel extends AbstractTableModel {

    private final Subscription subscription;
    private List<MergeTableColumn> columns = new ArrayList<>();

    private int rowCount = 200;

    public MergeTableModel(MergeModelStore store) {
        subscription = store.getRowMatching().subscribe(new Observer<RowMatching>() {
            @Override
            public void onChange(Observable<RowMatching> observable) {
                if(observable.isLoading()) {
                    rowCount = 0;
                } else {
                    rowCount = observable.get().getRowCount();
                }
                fireTableDataChanged();
            }
        });
    }


    public void updateColumns(List<MergeTableColumn> columns) {
        this.columns = columns;
        fireTableStructureChanged();
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
        return columns.get(columnIndex).getValue(rowIndex);
    }

    public void stop() {
        subscription.unsubscribe();
    }
}
