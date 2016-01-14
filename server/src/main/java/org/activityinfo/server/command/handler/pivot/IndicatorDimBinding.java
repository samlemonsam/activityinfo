package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;

public class IndicatorDimBinding {
    
    private final Dimension model = new Dimension(DimensionType.Indicator);
    
    public Dimension getModel() {
        return model;
    }
    
    public DimensionCategory category(IndicatorMetadata indicator) {
        return new EntityCategory(
                indicator.getDestinationId(), 
                indicator.getName(), 
                indicator.getSortOrder());
    }

}
