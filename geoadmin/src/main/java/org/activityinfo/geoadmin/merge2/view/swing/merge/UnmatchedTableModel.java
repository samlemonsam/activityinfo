package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.view.model.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.model.FormProfile;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Created by alex on 22-5-15.
 */
public class UnmatchedTableModel extends AbstractTableModel {
    
    private List<FieldProfile> fields;
    private int rowCount;
    
    public void update(List<FieldProfile> fields, int rowCount) {
        this.fields = fields;
        this.rowCount = rowCount;
    }
    
    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        if(fields == null) {
            return 0;
        }
        return fields.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (fields != null) {
            FieldProfile field = fields.get(columnIndex);
            if (field != null && field.getView() != null) {
                return field.getView().get(rowIndex);
            }
        }
        return null;
    }
}
