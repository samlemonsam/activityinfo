package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

public class ProjectDimBinding extends DimBinding {


    private static final String PROJECT_ID_COLUMN = "ProjectId";
    private static final String PROJECT_LABEL_COLUMN = "ProjectLabel";

    private final Dimension model = new Dimension(DimensionType.Project);

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return getColumnQuery();
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return getColumnQuery();
    }

    private List<ColumnModel> getColumnQuery() {
        ColumnModel projectId = new ColumnModel();
        projectId.setExpression(new SymbolExpr("project"));
        projectId.setId(PROJECT_ID_COLUMN);

        ColumnModel projectLabel = new ColumnModel();
        projectLabel.setExpression(new CompoundExpr(new SymbolExpr("project"), new SymbolExpr("label")));
        projectLabel.setId(PROJECT_LABEL_COLUMN);

        return Arrays.asList(projectId, projectLabel);
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, ColumnSet columnSet) {
        return extractEntityCategories(columnSet, PROJECT_ID_COLUMN, PROJECT_LABEL_COLUMN);
    }

    @Override
    public DimensionCategory extractTargetCategory(ActivityMetadata activity, ColumnSet columnSet, int rowIndex) {
        return extractEntityCategory(
                columnSet.getColumnView(PROJECT_ID_COLUMN),
                columnSet.getColumnView(PROJECT_LABEL_COLUMN), rowIndex);
    }
}
