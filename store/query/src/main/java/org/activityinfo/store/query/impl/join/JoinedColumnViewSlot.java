package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.store.query.impl.views.JoinedColumnView;
import org.activityinfo.store.query.impl.views.StringArrayColumnView;

import java.util.List;


public class JoinedColumnViewSlot implements Slot<ColumnView> {

    private List<JoinLink> links;
    private Slot<ColumnView> nestedColumn;

    private ColumnView result;

    public JoinedColumnViewSlot(List<JoinLink> links, Slot<ColumnView> nestedColumn) {
        this.links = links;
        this.nestedColumn = nestedColumn;
    }

    @Override
    public ColumnView get() {
        if(result == null) {
            result = join();
        }
        return result;
    }

    private ColumnView join() {


        // build a vector each link that maps each row index from
        // the left table to the corresponding index in the table
        // containing our _nestedColumn_ that we want to join

        // So if LEFT is our base table, and RIGHT is the table that
        // contains _column_ that we want to join, then for each row i,
        // in the LEFT table, mapping[i] gives us the corresponding
        // row in the RIGHT table.

        int left[] = links.get(0).buildMapping();


        // If we have intermediate tables, we have to follow the links...

        for(int j=1;j<links.size();++j) {
            int right[] = links.get(j).buildMapping();
            for(int i=0;i!=left.length;++i) {
                if(left[i] != -1) {
                    left[i] = right[left[i]];
                }
            }
        }

        switch (nestedColumn.get().getType()) {
            case STRING:
                return joinStringColumn(nestedColumn.get(), left);
            case NUMBER:
                return joinDoubleColumn(nestedColumn.get(), left);
            case BOOLEAN:
                break;
            case GEOGRAPHIC:
                break;
        }
        
        return new JoinedColumnView(nestedColumn.get(), left);
    }
    private ColumnView joinStringColumn(ColumnView columnView, int[] joinMap) {
        int numRows = joinMap.length;
        String[] array = new String[numRows];
        for(int row=0;row< numRows;++row) {
            int right = joinMap[row];
            if(right != -1) {
                array[row] = columnView.getString(right);
            }
        }
        return new StringArrayColumnView(array);        
    }
    
    private ColumnView joinDoubleColumn(ColumnView columnView, int[] joinMap) {
        int numRows = joinMap.length;
        double[] array = new double[numRows];
        for(int row=0;row< numRows;++row) {
            int right = joinMap[row];
            if(right != -1) {
                array[row] = columnView.getDouble(right);
            }
        }
        return new DoubleArrayColumnView(array);    
    }
}
