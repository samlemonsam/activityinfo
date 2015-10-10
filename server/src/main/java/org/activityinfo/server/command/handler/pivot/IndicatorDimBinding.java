package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;

public class IndicatorDimBinding {
    
    private final Dimension model = new Dimension(DimensionType.Indicator);
    
    public Dimension getModel() {
        return model;
    }
    
    public DimensionCategory category(FormTree formTree, int indicatorId) {
        FormTree.Node fieldNode = formTree.getRootField(CuidAdapter.indicatorField(indicatorId));
        String label = fieldNode.getField().getLabel();
        int sortOrder = findSortOrder(formTree, fieldNode);
        
        return new EntityCategory(indicatorId, label, sortOrder);
    }

    private int findSortOrder(FormTree formTree, FormTree.Node fieldNode) {
        return formTree.getRootFields().indexOf(fieldNode) + 1;
    }
}
