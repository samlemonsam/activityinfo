package org.activityinfo.ui.client.table.view;

import com.google.common.collect.Multimap;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import org.activityinfo.model.expr.ExprNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Translates between GXT's FilterConfig and ActivityInfo formulas
 */
public class ColumnFilterParser {

    private Set<ExprNode> columns = new HashSet<>();

    public void addColumn(ExprNode column) {
        columns.add(column);
    }

    /**
     * Use recursive descent parsing to TRY to parse a formula as list of
     * GXT Filter configs. Not all formulas are in this format. For example,
     * if there are two columns A and B, and the filter is "A > B", then there is
     * no way to represent this using the GXT UI. (We will need some sort of "advance" filter
     * bar in the future)
     *
     * @param filter
     */
    public void parse(ExprNode filter) {
        throw new UnsupportedOperationException("TODO");
    }
}
