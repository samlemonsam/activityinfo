package org.activityinfo.analysis.table;

import com.google.common.base.Optional;
import org.activityinfo.model.formula.FormulaNode;

public interface TableUpdater {

    void updateFilter(Optional<FormulaNode> filterFormula);

    void updateColumnWidth(String columnId, int width);
}
