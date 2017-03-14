package org.activityinfo.ui.client.analysis.view;

import com.sencha.gxt.core.client.ValueProvider;
import org.activityinfo.ui.client.analysis.viewModel.Point;


class PointDimProvider implements ValueProvider<Point, String> {

    private int dimensionIndex;

    public PointDimProvider(int dimensionIndex) {
        this.dimensionIndex = dimensionIndex;
    }


    @Override
    public String getValue(Point object) {
        return object.getDimension(dimensionIndex);
    }

    @Override
    public void setValue(Point object, String value) {
    }

    @Override
    public String getPath() {
        return "dimension" + dimensionIndex;
    }
}
