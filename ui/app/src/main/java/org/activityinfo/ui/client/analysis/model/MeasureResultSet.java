package org.activityinfo.ui.client.analysis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The result set of a single measure.
 */
public class MeasureResultSet {

    private DimensionSet dimensions;
    private List<Point> points;

    public MeasureResultSet() {
    }

    public MeasureResultSet(DimensionSet dimensions, List<Point> points) {
        this.dimensions = dimensions;
        this.points = points;
    }

    public MeasureResultSet(DimensionSet dimensions, Point point) {
        this.dimensions = dimensions;
        this.points = new ArrayList<>();
        points.add(point);
    }

    public List<Point> getPoints() {
        return points;
    }


    public DimensionSet getDimensions() {
        return dimensions;
    }
}
