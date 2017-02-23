package org.activityinfo.legacy.shared.impl.pivot.calc;

import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;

import java.util.Map;

public class ActivityAccessor implements DimAccessor {

    private Dimension dim;
    private final Map<Integer, DimensionCategory> categoryMap;

    public ActivityAccessor(Dimension dim, Map<Integer, DimensionCategory> categoryMap) {
        this.dim = dim;
        this.categoryMap = categoryMap;
    }


    @Override
    public Dimension getDimension() {
        return dim;
    }

    @Override
    public DimensionCategory getCategory(SiteDTO siteDTO) {
        int activityId = siteDTO.getActivityId();
        return categoryMap.get(activityId);
    }
}
