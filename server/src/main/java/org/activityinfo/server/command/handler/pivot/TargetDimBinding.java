package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.TargetCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.store.mysql.metadata.Activity;

import java.util.Arrays;

/**
 * Maps target
 */
public class TargetDimBinding extends DimBinding {
    private final Dimension model = new Dimension(DimensionType.Target);

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(Activity activity, ColumnSet columnSet) {
        
        DimensionCategory categories[] = new DimensionCategory[columnSet.getNumRows()];
        Arrays.fill(categories, TargetCategory.REALIZED);
        
        return categories;
    }

    @Override
    public DimensionCategory extractTargetCategory(Activity activity, ColumnSet columnSet, int rowIndex) {
        return TargetCategory.TARGETED;
    }
}
