package org.activityinfo.analysis.table;

import com.google.common.base.Optional;
import org.activityinfo.model.expr.ExprNode;

public interface TableUpdater {

    void updateFilter(Optional<ExprNode> filterFormula);

    void updateColumnWidth(String columnId, int width);
}
