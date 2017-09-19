package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;

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

    private Grid<Integer> grid;

    @Override
    protected void onStateChange(Filter<Integer, ?> filter) {
        super.onStateChange(filter);
        LOGGER.info("Filter updated: " + FilterConfigParser.toFormula(buildQuery(getFilterData())).asExpression());
    }


}
