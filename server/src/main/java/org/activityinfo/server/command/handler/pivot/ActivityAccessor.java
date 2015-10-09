package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;

import java.util.Arrays;

public class ActivityAccessor extends DimensionAccessor {
   
    private final Dimension model = new Dimension(DimensionType.Activity);


    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(FormTree formTree, ColumnSet columnSet) {
        int activityId = activityIdOf(formTree);
        String name = formTree.getRootFormClass().getLabel();

        // TODO: Sort order!!!
        DimensionCategory[] categories = new DimensionCategory[columnSet.getNumRows()];
        Arrays.fill(categories, new EntityCategory(activityId, name));
        
        return categories;
    }
}
