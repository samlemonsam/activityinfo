package org.activityinfo.legacy.shared.impl.pivot.calc;

import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;

public class PartnerAccessor implements DimAccessor {

    private final Dimension dimension;

    public PartnerAccessor(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public Dimension getDimension() {
        return dimension;
    }

    @Override
    public DimensionCategory getCategory(SiteDTO siteDTO) {
        return new EntityCategory(siteDTO.getPartnerId(), siteDTO.getPartnerName());
    }
}
