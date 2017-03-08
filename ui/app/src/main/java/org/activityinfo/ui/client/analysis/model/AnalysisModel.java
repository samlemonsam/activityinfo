package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.resource.ResourceId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines a pivot model from one or more data sources.
 */
public class AnalysisModel {

    private final List<MeasureModel> measures = new ArrayList<>();
    private final List<DimensionModel> dimensions = new ArrayList<>();

    public AnalysisModel() {
    }

    public List<MeasureModel> getMeasures() {
        return measures;
    }

    public List<DimensionModel> getDimensions() {
        return dimensions;
    }

    public Set<ResourceId> formIds() {
        Set<ResourceId> ids = new HashSet<>();
        for (MeasureModel measure : measures) {
            ids.add(measure.getFormId());
        }
        return ids;
    }
}
