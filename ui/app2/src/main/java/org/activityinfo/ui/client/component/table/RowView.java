package org.activityinfo.ui.client.component.table;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yuriyz on 5/17/2016.
 */
public class RowView {

    private final Map<String, ColumnView> data;
    private final int row;

    public RowView(int row, Map<String, ColumnView> data) {
        this.data = data;
        this.row = row;
    }

    public String getId() {
        return data.get("id").getString(row);
    }

    public ResourceId getResourceId() {
        return ResourceId.valueOf(getId());
    }

    public Object getValue(String columnKey) {
        return data.get(columnKey).get(row);
    }

    public static Collection<RowView> asRowViews(ColumnSet columnSet) {
        List<RowView> rows = Lists.newArrayList();
        for (int row = 0; row < columnSet.getNumRows(); row++) {
            rows.add(new RowView(row, columnSet.getColumns()));
        }
        return rows;
    }

}
