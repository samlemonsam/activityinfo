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
//        return model.getLabel().transform(label -> new MeasureListItem(model, label));
        throw new UnsupportedOperationException();
    }


    public String getLabel() {
        return label;
    }

    public String getId() {
        return model.getId();
    }

    public MeasureModel getModel() {
        return model;
    }
}
