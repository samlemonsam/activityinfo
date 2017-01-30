package org.activityinfo.ui.client.measureDialog.model;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.analysis.model.MeasureModel;

public class CalculationMeasureType implements MeasureType {


    @Override
    public String getId() {
        return "calculation";
    }

    @Override
    public String getLabel() {
        return "Calculation";
    }

    @Override
    public MeasureModel buildModel(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CalculationMeasureType;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
