package org.activityinfo.analysis.table;

import com.google.common.base.Optional;
import org.activityinfo.model.expr.ExprNode;

public interface FilterUpdater {

    void updateFilter(Optional<ExprNode> filterFormula);
}
