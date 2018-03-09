package org.activityinfo.ui.client.table.view;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import org.activityinfo.analysis.table.TableUpdater;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.Formulas;

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

    private final List<ColumnView> columns = new ArrayList<>();
    private final ColumnFilterParser filterParser;
    private final TableUpdater tableUpdater;

    private Optional<String> currentFilter = Optional.absent();

    public TableGridFilters(TableUpdater tableUpdater) {
        this.tableUpdater = tableUpdater;
        this.filterParser = new ColumnFilterParser();
        setAutoReload(false);
    }

    public void addFilter(ColumnView filter) {
        super.addFilter(filter.getFilterView());
        columns.add(filter);
        filterParser.addColumn(filter.getColumnFormula());
    }

    public Optional<FormulaNode> buildFormula() {
        List<FormulaNode> nodes = new ArrayList<>();
        for (ColumnView filter : columns) {
            if(filter.isFilterActive()) {
                nodes.add(filter.getFilterFormula());
            }
        }
        if(nodes.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(Formulas.allTrue(nodes));
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
        changeFilter(filter);
    }


    private void changeFilter(Filter<Integer, ?> filter) {
        super.onStateChange(filter);

        Optional<FormulaNode> filterFormula = buildFormula();

        LOGGER.info("Filter updated: " + filterFormula);

        tableUpdater.updateFilter(filterFormula);
    }


    /**
     * This method is called when the MODEL has changed.
     *
     * Update the user interface to match the model's state.
     */
    void updateView(Optional<String> filter) {

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
