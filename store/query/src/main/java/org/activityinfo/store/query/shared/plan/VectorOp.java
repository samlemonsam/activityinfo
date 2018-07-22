package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.formula.functions.FormulaFunction;

import java.util.List;

public class VectorOp extends ColumnPlanNode {

    private ColumnFunction function;
    private List<ColumnPlanNode> columns;

    public VectorOp(ColumnFunction function, List<ColumnPlanNode> columns) {
        this.function = function;
        this.columns = columns;
    }

    @Override
    public String getDebugLabel() {
        return ((FormulaFunction) function).getId();
    }

    @Override
    public List<? extends PlanNode> getInputs() {
        return columns;
    }
}
