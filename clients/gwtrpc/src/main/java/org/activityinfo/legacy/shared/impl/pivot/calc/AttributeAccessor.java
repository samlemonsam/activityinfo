package org.activityinfo.legacy.shared.impl.pivot.calc;

import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.AttributeCategory;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.AttributeGroupDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;

import java.util.Collection;

public class AttributeAccessor implements DimAccessor {

    private AttributeGroupDimension dim;
    private Collection<EntityCategory> attributes;

    public AttributeAccessor(AttributeGroupDimension dim, Collection<EntityCategory> attributes) {
        this.dim = dim;
        this.attributes = attributes;
    }

    @Override
    public Dimension getDimension() {
        return dim;
    }

    @Override
    public DimensionCategory getCategory(SiteDTO siteDTO) {
        for(EntityCategory attribute : attributes) {
            if(siteDTO.getAttributeValue(attribute.getId())) {
                return new AttributeCategory(attribute.getLabel(), attribute.getSortOrder());
            }
        }
        return null;
    }
}
