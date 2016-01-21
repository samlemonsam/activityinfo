package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;


public class LocationDimBinding extends DimBinding {

    private static final String ID_COLUMN = "LocationId";
    private static final String LABEL_COLUMN = "LocationName";
   
    private final Dimension model = new Dimension(DimensionType.Location);

    public LocationDimBinding() {
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        ResourceId locationFieldId = CuidAdapter.locationField(activityIdOf(formTree));
        return Arrays.asList(
                new ColumnModel().setExpression(locationFieldId).as(ID_COLUMN),
                new ColumnModel().setExpression(new CompoundExpr(locationFieldId, "label")).as(LABEL_COLUMN));
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, ColumnSet columnSet) {
        return extractEntityCategories(columnSet, ID_COLUMN, LABEL_COLUMN);
    }
}
