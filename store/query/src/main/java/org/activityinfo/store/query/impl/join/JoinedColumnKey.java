package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.FilterLevel;
import org.activityinfo.store.query.impl.Slot;

import java.util.List;

public class JoinedColumnKey {

    private final FilterLevel filterLevel;
    private final List<ReferenceJoin> links;
    private final Slot<ColumnView> nestedColumn;

    public JoinedColumnKey(FilterLevel filterLevel, List<ReferenceJoin> links, Slot<ColumnView> nestedColumn) {
        this.filterLevel = filterLevel;
        this.links = links;
        this.nestedColumn = nestedColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinedColumnKey key = (JoinedColumnKey) o;

        if (filterLevel != key.filterLevel) return false;
        if (!links.equals(key.links)) return false;
        return nestedColumn.equals(key.nestedColumn);

    }

    @Override
    public int hashCode() {
        int result = filterLevel.hashCode();
        result = 31 * result + links.hashCode();
        result = 31 * result + nestedColumn.hashCode();
        return result;
    }
}
