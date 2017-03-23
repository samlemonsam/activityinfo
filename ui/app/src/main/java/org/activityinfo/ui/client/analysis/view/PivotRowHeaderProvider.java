package org.activityinfo.ui.client.analysis.view;


import com.sencha.gxt.core.client.ValueProvider;

class PivotRowHeaderProvider implements ValueProvider<PivotRow, String> {
    private int rowDimensionIndex;

    public PivotRowHeaderProvider(int rowDimensionIndex) {
        this.rowDimensionIndex = rowDimensionIndex;
    }

    @Override
    public String getValue(PivotRow object) {
        return object.getRowHeader(rowDimensionIndex);
    }

    @Override
    public void setValue(PivotRow object, String value) {
    }

    @Override
    public String getPath() {
        return "rh" + rowDimensionIndex;
    }
}
