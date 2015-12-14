package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;

public class IndicatorDimBinding {
    
    private final Dimension model = new Dimension(DimensionType.Indicator);
    
    public Dimension getModel() {
        return model;
    }
    
    public DimensionCategory category(FormTree formTree, IndicatorMetadata indicator) {
        FormTree.Node fieldNode = formTree.getRootField(indicator.getFieldId());
        String label = fieldNode.getField().getLabel();
        int sortOrder = indicator.sortOrder;
        
        return new EntityCategory(indicator.getId(), label, sortOrder);
    }

    private int findSortOrder(FormTree formTree, FormTree.Node fieldNode) {
        return formTree.getRootFields().indexOf(fieldNode) + 1;
    }
}
