package org.activityinfo.ui.client.analysis.view;

import com.sencha.gxt.core.client.ValueProvider;
import org.activityinfo.ui.client.analysis.viewModel.Point;

public class PointStatProvider implements ValueProvider<Point, String> {
    @Override
    public String getValue(Point point) {
        return point.getStatistic().getLabel();
    }

    @Override
    public void setValue(Point object, String value) {

    }

    @Override
    public String getPath() {
        return "stat";
    }
}
