package org.activityinfo.legacy.shared.impl.pivot.calc;

import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.AdminDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;

public class AdminAccessor implements DimAccessor {

    private int adminLevelId;
    private AdminDimension dim;

    public AdminAccessor(AdminDimension dim) {
        this.dim = dim;
        adminLevelId = dim.getLevelId();
    }

    @Override
    public Dimension getDimension() {
        return dim;
    }

    @Override
    public DimensionCategory getCategory(SiteDTO siteDTO) {
        AdminEntityDTO entityDTO = siteDTO.getAdminEntity(adminLevelId);
        if(entityDTO == null) {
            return null;
        } else {
            return new EntityCategory(entityDTO.getId(), entityDTO.getName());
        }
    }
}
