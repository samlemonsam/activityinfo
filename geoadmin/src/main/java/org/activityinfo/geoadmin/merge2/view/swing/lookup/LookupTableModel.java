package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.view.mapping.LookupTable;
import org.activityinfo.geoadmin.merge2.view.mapping.ReferenceFieldMapping;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.observable.Subscription;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class LookupTableModel extends AbstractTableModel {

    private final ReferenceFieldMapping mapping;
    private final List<String> columnNames = new ArrayList<>();
    private final List<Observable<ColumnView>> columns = new ArrayList<>();
    
    private Subscription subscription;


    public LookupTableModel(ReferenceFieldMapping mapping, Observable<LookupTable> lookupTable) {
        this.mapping = mapping;

        for(int i=0;i!=mapping.getSourceKeyFields().size();++i) {

            final int keyFieldIndex = i;
            
            // Add the source/target columns for comparison for each matching
            columnNames.add(mapping.getSourceKeyFields().get(i).getLabel());
            columns.add(lookupTable.transform(new Function<LookupTable, ColumnView>() {
                @Override
                public ColumnView apply(LookupTable input) {
                    return input.getSourceView(keyFieldIndex);
                }
            }));

            columnNames.add(mapping.getTargetKeyFields().get(i).getLabel());
            columns.add(lookupTable.transform(new Function<LookupTable, ColumnView>() {
                @Override
                public ColumnView apply(LookupTable input) {
                    return input.getTargetView(keyFieldIndex);
                }
            }));
        }
        
        subscription = Observable.flatten(columns).subscribe(new Observer<List<ColumnView>>() {
            @Override
            public void onChange(Observable<List<ColumnView>> observable) {
                fireTableDataChanged();
            }
        });
    }

    @Override
    public int getRowCount() {
        return mapping.getSourceKeySet().size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Observable<ColumnView> view = columns.get(columnIndex);
        if (view.isLoading()) {
            return null;
        } else {
            return view.get().getString(rowIndex);
        }
    }
    
    public void stop() {
        subscription.unsubscribe();
    }
}
