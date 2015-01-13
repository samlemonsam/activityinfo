package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.Slot;
import org.activityinfo.store.query.impl.views.JoinedColumnView;

import java.util.List;

/**
* Created by alex on 1/13/15.
*/
public class Join implements Slot<ColumnView> {

    private List<JoinLink> links;
    private Slot<ColumnView> nestedColumn;

    private ColumnView result;

    public Join(List<JoinLink> links, Slot<ColumnView> nestedColumn) {
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

        return new JoinedColumnView(nestedColumn.get(), left);
    }
}
