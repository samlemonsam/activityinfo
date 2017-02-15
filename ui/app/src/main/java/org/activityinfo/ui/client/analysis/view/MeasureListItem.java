package org.activityinfo.ui.client.analysis.view;

import org.activityinfo.ui.client.analysis.model.MeasureModel;

public class MeasureListItem {
    private MeasureModel model;
    private String label;

    public MeasureListItem(MeasureModel model) {
        this.model = model;
        this.label = model.getLabel().get();
    }

    public MeasureModel getModel() {
        return model;
    }

    public String getLabel() {
        return label;
    }

    public String getKey() {
        return model.getKey();
    }
}
