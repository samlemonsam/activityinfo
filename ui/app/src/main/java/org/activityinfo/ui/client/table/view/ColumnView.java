package org.activityinfo.ui.client.table.view;

import com.google.common.collect.Lists;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.Formulas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wraps a GXT filter with the expression used for the column.
 */
public class ColumnView {

    private final FormulaNode columnFormula;
    private final Filter<Integer, ?> filterView;

    /**
     * True if the view currently has no active filter set.
     */
    private boolean emptyFilter = true;

    public ColumnView(ParsedFormula formula, Filter<Integer, ?> filter) {
        this.columnFormula = formula.getRootNode();
        this.filterView = filter;
    }

    public FormulaNode getColumnFormula() {
        return columnFormula;
    }

    public Filter<Integer, ?> getFilterView() {
        return filterView;
    }

    public boolean isFilterActive() {
        return filterView.isActive();
    }

    public FormulaNode getFilterFormula() {
        return toFormula(columnFormula, filterView.getFilterConfig());
    }

    public static FormulaNode toFormula(FormulaNode field, List<FilterConfig> filters) {
        List<FormulaNode> filterExprs = new ArrayList<>();
        for (FilterConfig filter : filters) {
            filterExprs.add(ColumnFilterParser.toFormula(field, filter));
        }
        return Formulas.allTrue(filterExprs);
    }


    public void updateView(Collection<FilterConfig> filterConfigs) {
        boolean suppressEvents = true;
        if(filterConfigs.isEmpty()) {
            if(!emptyFilter) {
                filterView.setActive(false, suppressEvents);
                emptyFilter = true;
            }
        } else {
            filterView.setActive(true, suppressEvents);
            filterView.setFilterConfig(Lists.newArrayList(filterConfigs));
            emptyFilter = false;
        }

    }
}
