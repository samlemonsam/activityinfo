package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.SimpleCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.store.mysql.metadata.Activity;

import java.util.Arrays;


public class ActivityCategoryDimBinding extends DimBinding {
   
    private Dimension model = new Dimension(DimensionType.ActivityCategory);
   
    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(Activity activity, ColumnSet columnSet) {

        DimensionCategory[] categories = new DimensionCategory[columnSet.getNumRows()];
        Arrays.fill(categories, categoryOf(activity));

        return categories;    
    }

    private SimpleCategory categoryOf(Activity activity) {
        if(activity.hasCategory()) {
            return new SimpleCategory(activity.getCategory());
        } else {
            return null;
        }
    }

    @Override
    public DimensionCategory extractTargetCategory(Activity activity, ColumnSet columnSet, int rowIndex) {
        return categoryOf(activity);
    }
}
