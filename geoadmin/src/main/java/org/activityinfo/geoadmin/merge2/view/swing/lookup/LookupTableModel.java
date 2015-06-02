package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import org.activityinfo.geoadmin.merge2.view.mapping.LookupTable;
import org.activityinfo.model.query.ColumnView;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class LookupTableModel extends AbstractTableModel {
    
    private final LookupTable lookupTable;
    private final List<ColumnView> columns = new ArrayList<>();
    private final List<String> columnNames = new ArrayList<>();
    public LookupTableModel(LookupTable lookupTable) {
        this.lookupTable = lookupTable;
        for(int i=0;i!=lookupTable.getSourceKeyFields().size();++i) {
            
            // Add the source/target columns for comparison for each matching
            columns.add(lookupTable.getSourceView(i));
            columnNames.add(lookupTable.getSourceKeyFields().get(i).getLabel());
            
            columns.add(lookupTable.getTargetView(i));
            columnNames.add(lookupTable.getTargetKeyFields().get(i).getLabel());
        }
    }

    @Override
    public int getRowCount() {
        return lookupTable.getRowCount();
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
        return columns.get(columnIndex).getString(rowIndex);
    }



}
