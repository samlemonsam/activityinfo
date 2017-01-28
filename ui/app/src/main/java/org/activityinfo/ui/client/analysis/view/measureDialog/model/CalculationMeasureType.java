package org.activityinfo.ui.client.analysis.view.measureDialog.model;

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
    public boolean equals(Object o) {
        return o instanceof CalculationMeasureType;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
