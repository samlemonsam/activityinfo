package org.activityinfo.server.command.handler.pivot;


import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;

import java.util.Collections;
import java.util.List;

public class SiteAccessor extends DimensionAccessor {

    private static final String COLUMN_ID = "SiteId";
    
    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return Collections.singletonList(new ColumnModel().setExpression(ColumnModel.ID_SYMBOL).as(COLUMN_ID));
    }

    @Override
    public Dimension getModel() {
        return null;
    }

    @Override
    public DimensionCategory[] extractCategories(FormTree formTree, ColumnSet columnSet) {
        return new DimensionCategory[0];
    }
}
