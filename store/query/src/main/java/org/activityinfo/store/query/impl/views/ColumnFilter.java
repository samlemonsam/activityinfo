package org.activityinfo.store.query.impl.views;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.QuerySyntaxException;
import org.activityinfo.store.query.impl.Slot;

import java.util.Arrays;


public class ColumnFilter implements Function<ColumnView, ColumnView> {

    private final Slot<ColumnView> filterColumn;
    private Optional<Slot<ColumnView>> parentKey;
    private int[] includeMap;

    /**
     * @param filterColumn supplier of a boolean-valued column that indicates which rows should
     *                   be included in the result set
     * @param parentKey the parent of the
     */
    public ColumnFilter(Slot<ColumnView> filterColumn, Optional<Slot<ColumnView>> parentKey) {
        this.filterColumn = filterColumn;
        this.parentKey = parentKey;
    }

    private void computeIndices() {
        if(includeMap == null) {
            ColumnView view = filterColumn.get();
            ColumnView parent = null;

            if (parentKey.isPresent()) {
                parent = parentKey.get().get();
            }

            if (view.getType() != ColumnType.BOOLEAN) {
                throw new QuerySyntaxException("Filter expression must evaluate to a boolean-valued column, got: " + view.getType());
            }
            int[] filtered = new int[view.numRows()];
            int filteredRowIndex = 0;
            for (int rowIndex = 0; rowIndex != view.numRows(); ++rowIndex) {
                if ( (parent == null || parent.getBoolean(rowIndex) == ColumnView.TRUE) &&
                        view.getBoolean(rowIndex) == ColumnView.TRUE) {
                    filtered[filteredRowIndex++] = rowIndex;
                }
            }
            int numFilteredRows = filteredRowIndex;
            includeMap = Arrays.copyOf(filtered, numFilteredRows);
        }
    }

    @Override
    public ColumnView apply(ColumnView input) {
        computeIndices();
        return new FilteredColumnView(input, includeMap);
    }
}
