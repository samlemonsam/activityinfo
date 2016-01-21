package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.query.ColumnSet;

import java.util.Arrays;

public class ActivityDimBinding extends DimBinding {
   
    private final Dimension model = new Dimension(DimensionType.Activity);


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

    @Override
    public DimensionCategory extractTargetCategory(ActivityMetadata activity, ColumnSet columnSet, int rowIndex) {
        return categoryOf(activity);
    }

    private EntityCategory categoryOf(ActivityMetadata activity) {
        return new EntityCategory(activity.getId(), activity.getName(), activity.getSortOrder());
    }
}
