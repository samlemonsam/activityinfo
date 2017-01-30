package org.activityinfo.ui.client.analysis.model;

import java.util.ArrayList;
import java.util.List;


public class DimensionSourceSet {

    private List<DimensionSourceModel> sources = new ArrayList<>();

    public DimensionSourceSet(List<List<DimensionSourceModel>> sourceLists) {
        for (List<DimensionSourceModel> sourceList : sourceLists) {
            sources.addAll(sourceList);
        }
    }

    public List<DimensionSourceModel> getSources() {
        return sources;
    }
}
