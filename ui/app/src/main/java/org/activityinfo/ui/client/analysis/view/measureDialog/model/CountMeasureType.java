package org.activityinfo.ui.client.analysis.view.measureDialog.model;

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
    public boolean equals(Object o) {
        return o instanceof CountMeasureType;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
