package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.Slot;

import java.util.List;

public class JoinedColumnKey {

    private final List<ReferenceJoin> links;
    private final Slot<ColumnView> nestedColumn;

    public JoinedColumnKey(List<ReferenceJoin> links, Slot<ColumnView> nestedColumn) {
        this.links = links;
        this.nestedColumn = nestedColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinedColumnKey that = (JoinedColumnKey) o;

        if (!links.equals(that.links)) return false;
        return nestedColumn.equals(that.nestedColumn);
    }

    @Override
    public int hashCode() {
        int result = links.hashCode();
        result = 31 * result + nestedColumn.hashCode();
        return result;
    }
}
