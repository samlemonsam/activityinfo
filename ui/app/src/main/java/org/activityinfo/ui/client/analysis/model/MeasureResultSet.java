package org.activityinfo.ui.client.analysis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The result set of a single measure.
 */
public class MeasureResultSet {

    private List<Point> points = new ArrayList<>();

    public MeasureResultSet() {
    }

    public MeasureResultSet(Point point) {
        points.add(point);
    }

    public List<Point> getPoints() {
        return points;
    }
}
