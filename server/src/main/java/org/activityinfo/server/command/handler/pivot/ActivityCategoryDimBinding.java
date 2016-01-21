package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.SimpleCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.query.ColumnSet;

import java.util.Arrays;


public class ActivityCategoryDimBinding extends DimBinding {
   
    private Dimension model = new Dimension(DimensionType.ActivityCategory);
   
    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, ColumnSet columnSet) {

        DimensionCategory[] categories = new DimensionCategory[columnSet.getNumRows()];
        Arrays.fill(categories, categoryOf(activity));

        return categories;    
    }

    private SimpleCategory categoryOf(ActivityMetadata activity) {
        return new SimpleCategory(activity.getCategoryName());
    }

    @Override
    public DimensionCategory extractTargetCategory(ActivityMetadata activity, ColumnSet columnSet, int rowIndex) {
        return categoryOf(activity);
    }
}
