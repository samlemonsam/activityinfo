package org.activityinfo.legacy.shared.impl.pivot.calc;

import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;

public interface DimAccessor {

    Dimension getDimension();

    DimensionCategory getCategory(SiteDTO siteDTO);
}
