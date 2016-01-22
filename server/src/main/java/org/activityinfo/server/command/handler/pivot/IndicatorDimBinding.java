package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.store.mysql.metadata.ActivityField;

public class IndicatorDimBinding {
    
    private final Dimension model = new Dimension(DimensionType.Indicator);
    
    public Dimension getModel() {
        return model;
    }
    
    public DimensionCategory category(ActivityField indicator) {
        return new EntityCategory(
                indicator.getId(), 
                indicator.getFormField().getLabel(), 
                indicator.getSortOrder());
    }

}
