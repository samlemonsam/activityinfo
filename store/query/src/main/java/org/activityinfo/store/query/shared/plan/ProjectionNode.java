package org.activityinfo.store.query.shared.plan;

import java.util.List;

/**
 * Combine one or more columns together into a table
 *
 * INPUT: list(columNodes)
 *
 *
 */
public class ProjectionNode implements PlanNode {

    private List<ColumnPlanNode> columns;

    public ProjectionNode(List<ColumnPlanNode> columns) {
        this.columns = columns;
    }

    public List<ColumnPlanNode> getColumns() {
        return columns;
    }

    @Override
    public String getDebugLabel() {
        return "project";
    }

    @Override
    public List<ColumnPlanNode> getInputs() {
        return columns;
    }
}
