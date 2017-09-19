package org.activityinfo.ui.client.table.view;

import com.google.common.base.Optional;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import org.activityinfo.analysis.table.FilterUpdater;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.Exprs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Replacement for {@link com.sencha.gxt.widget.core.client.grid.filters.AbstractGridFilters}, which maintains
 * the filter state internally.
 *
 *
 * <p>We want to use the GXT filter UI, but apply the changes to the TableModel rather than hoard the state here.
 */
public class TableGridFilters extends GridFilters<Integer> {

    private static final Logger LOGGER = Logger.getLogger(TableGridFilters.class.getName());

    private FilterUpdater filterUpdater;
    private List<ColumnFilter> filters = new ArrayList<>();

    public TableGridFilters(FilterUpdater filterUpdater) {
        this.filterUpdater = filterUpdater;
    }


    public void addFilter(ColumnFilter filter) {
        super.addFilter(filter.getFilter());
        filters.add(filter);
    }

    public Optional<ExprNode> buildFormula() {
        List<ExprNode> nodes = new ArrayList<>();
        for (ColumnFilter filter : filters) {
            if(filter.isActive()) {
                nodes.add(filter.getFilterFormula());
            }
        }
        if(nodes.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(Exprs.allTrue(nodes));
        }
    }

    @Override
    protected void onStateChange(Filter<Integer, ?> filter) {
        Optional<ExprNode> filterFormula = buildFormula();

        LOGGER.info("Filter updated: " + filterFormula);

        filterUpdater.updateFilter(filterFormula);
    }


}
