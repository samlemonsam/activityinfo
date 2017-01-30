package org.activityinfo.ui.client.analysis.view;

import com.sencha.gxt.core.client.ValueProvider;
import org.activityinfo.ui.client.analysis.model.Point;

class PointValueProvider implements ValueProvider<Point, Double> {
    @Override
    public Double getValue(Point object) {
        return object.getValue();
    }

    @Override
    public void setValue(Point object, Double value) {
    }

    @Override
    public String getPath() {
        return "value";
    }
}
