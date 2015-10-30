package org.activityinfo.server.command.handler.pivot;


import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;

import java.util.Arrays;
import java.util.List;

public class SiteDimBinding extends DimBinding {

    private static final String ID_COLUMN = "SiteId";
    
    private static final String LABEL_COLUMN = "SiteName";
    
    private final Dimension model = new Dimension(DimensionType.Site);
    
    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return Arrays.asList(
                new ColumnModel().setExpression(ColumnModel.ID_SYMBOL).as(ID_COLUMN),
                new ColumnModel().setExpression(CuidAdapter.locationField(activityIdOf(formTree)) + ".name").as(LABEL_COLUMN));
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, FormTree formTree, ColumnSet columnSet) {
        return extractEntityCategories(columnSet, ID_COLUMN, LABEL_COLUMN);
    }
}
