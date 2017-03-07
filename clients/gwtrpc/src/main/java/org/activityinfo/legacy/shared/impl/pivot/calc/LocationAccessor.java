package org.activityinfo.legacy.shared.impl.pivot.calc;

import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;

public class LocationAccessor implements DimAccessor {

    private Dimension dim;

    public LocationAccessor(Dimension dim) {
        this.dim = dim;
    }

    @Override
    public Dimension getDimension() {
        return dim;
    }

    @Override
    public DimensionCategory getCategory(SiteDTO siteDTO) {
        return new EntityCategory(siteDTO.getLocationId(), siteDTO.getLocationName());
    }
}
