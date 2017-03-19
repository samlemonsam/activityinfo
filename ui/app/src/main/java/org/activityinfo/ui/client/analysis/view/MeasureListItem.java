package org.activityinfo.ui.client.analysis.view;

import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.analysis.viewModel.EffectiveMeasure;

public class MeasureListItem {
    private EffectiveMeasure model;

    public MeasureListItem(EffectiveMeasure model) {
        this.model = model;
    }

    public String getLabel() {
        return model.getModel().getLabel();
    }

    public String getId() {
        return model.getModel().getId();
    }

    public MeasureModel getModel() {
        return model.getModel();
    }
}
