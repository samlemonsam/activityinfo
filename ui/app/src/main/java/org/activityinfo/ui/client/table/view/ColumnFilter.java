package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.expr.ExprNode;

/**
 * Wraps a GXT filter with the expression used for the column.
 */
public class ColumnFilter {

    private final ExprNode columnFormula;
    private final Filter<Integer, ?> filter;

    public ColumnFilter(ParsedFormula formula, Filter<Integer, ?> filter) {
        this.columnFormula = formula.getRootNode();
        this.filter = filter;
    }

    public ExprNode getColumnFormula() {
        return columnFormula;
    }

    public Filter<Integer, ?> getFilter() {
        return filter;
    }

    public boolean isActive() {
        return filter.isActive();
    }

    public ExprNode getFilterFormula() {
        return FilterConfigParser.toFormula(columnFormula, filter.getFilterConfig());
    }
}
