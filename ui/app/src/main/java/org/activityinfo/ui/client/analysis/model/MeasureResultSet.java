package org.activityinfo.ui.client.analysis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The result set of a single measure.
 */
public class MeasureResultSet {

    private DimensionSet dimensions;
    private List<Point> points = new ArrayList<>();

    public MeasureResultSet() {
    }


    public MeasureResultSet(DimensionSet dimensions, Point point) {
        this.dimensions = dimensions;
        points.add(point);
    }

    public List<Point> getPoints() {
        return points;
    }


    public DimensionSet getDimensions() {
        return dimensions;
    }
}
