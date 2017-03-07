package org.activityinfo.ui.client.analysis.view;

import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.analysis.model.MeasureModel;

public class MeasureListItem {
    private MeasureModel model;
    private String label;

    private MeasureListItem(MeasureModel model, String label) {
        this.model = model;
        this.label = label;
    }

    public static Observable<MeasureListItem> compute(MeasureModel model) {
        return model.getLabel().transform(label -> new MeasureListItem(model, label));
    }


    public String getLabel() {
        return label;
    }

    public String getKey() {
        return model.getKey();
    }

    public MeasureModel getModel() {
        return model;
    }
}
