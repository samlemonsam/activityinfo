package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.SimpleCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;

import java.util.Arrays;


public class ActivityCategoryDimBinding extends DimBinding {
   
    private Dimension model = new Dimension(DimensionType.ActivityCategory);
   
    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, FormTree formTree, ColumnSet columnSet) {
        int activityId = activityIdOf(formTree);
        String name = formTree.getRootFormClass().getLabel();

        SimpleCategory category = new SimpleCategory(activity.getCategoryName());

        DimensionCategory[] categories = new DimensionCategory[columnSet.getNumRows()];
        Arrays.fill(categories, category);

        return categories;    
    }
}
