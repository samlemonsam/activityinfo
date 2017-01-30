package org.activityinfo.legacy.shared.impl.pivot.calc;

import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.TargetCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;

/**
 * For calculated indicators, provides the constant value of "Target" for
 * the Target/Realized dimension.
 */
public class TargetAccessor implements DimAccessor {
    private Dimension dim;

    public TargetAccessor(Dimension dim) {
        this.dim = dim;
    }

    @Override
    public Dimension getDimension() {
        return dim;
    }

    @Override
    public DimensionCategory getCategory(SiteDTO siteDTO) {
        return TargetCategory.REALIZED;
    }
}