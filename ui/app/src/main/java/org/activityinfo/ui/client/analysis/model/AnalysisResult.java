package org.activityinfo.ui.client.analysis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of the analysis
 */
public class AnalysisResult {

    private List<Point> points = new ArrayList<>();


    public AnalysisResult(List<MeasureResultSet> measureSets) {
        for (MeasureResultSet measureSet : measureSets) {
            points.addAll(measureSet.getPoints());
        }
    }

    public List<Point> getPoints() {
        return points;
    }
}
