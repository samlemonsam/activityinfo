package org.activityinfo.ui.client.table.view;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import org.activityinfo.analysis.table.FilterUpdater;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.Exprs;

import java.util.ArrayList;
import java.util.Collection;
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

    private final List<ColumnFilter> columns = new ArrayList<>();
    private final ColumnFilterParser filterParser;
    private final FilterUpdater filterUpdater;

    private Optional<String> currentFilter = Optional.absent();

    public TableGridFilters(FilterUpdater filterUpdater) {
        this.filterUpdater = filterUpdater;
        this.filterParser = new ColumnFilterParser();
        setAutoReload(false);
    }

    public void addFilter(ColumnFilter filter) {
        super.addFilter(filter.getView());
        columns.add(filter);
        filterParser.addColumn(filter.getColumnFormula());
    }

    public Optional<ExprNode> buildFormula() {
        List<ExprNode> nodes = new ArrayList<>();
        for (ColumnFilter filter : columns) {
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

    /**
     * This method is called when the user *INITIATES* a change to the filter
     * via the user interface.
     *
     * We do NOT want to mutate our state locally, we forward it to the TableModel.
     */
    @Override
    protected void onStateChange(Filter<Integer, ?> filter) {
        super.onStateChange(filter);

        Optional<ExprNode> filterFormula = buildFormula();

        LOGGER.info("Filter updated: " + filterFormula);

        filterUpdater.updateFilter(filterFormula);
    }


    /**
     * This method is called when the MODEL has changed.
     *
     * Update the user interface to match the model's state.
     */
    public void updateView(Optional<String> filter) {

        if(!filter.equals(currentFilter)) {
            Multimap<Integer, FilterConfig> map = filterParser.parseFilter(filter);
            for (int i = 0; i < columns.size(); i++) {
                Collection<FilterConfig> filterConfigs = map.get(i);
                columns.get(i).updateView(filterConfigs);
            }

            updateColumnHeadings();
            currentFilter = filter;
        }
    }
}
