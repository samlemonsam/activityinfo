package org.activityinfo.ui.client.analysis.view.measureDialog.model;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.analysis.model.CountMeasure;
import org.activityinfo.ui.client.analysis.model.MeasureModel;

public class CountMeasureType implements MeasureType {
    @Override
    public String getId() {
        return "count";
    }

    @Override
    public String getLabel() {
        return "Count";
    }

    @Override
    public MeasureModel buildModel(FormClass formClass) {
        return new CountMeasure(formClass.getId());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CountMeasureType;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
