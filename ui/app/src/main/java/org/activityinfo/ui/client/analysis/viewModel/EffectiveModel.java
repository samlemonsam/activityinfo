package org.activityinfo.ui.client.analysis.viewModel;


import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;

import java.util.ArrayList;
import java.util.List;

public class EffectiveModel {

    private DimensionSet dimensions;
    private List<EffectiveMeasure> measures = new ArrayList<>();

    public EffectiveModel(AnalysisModel model, FormForest formForest) {
        this.dimensions = new DimensionSet(model.getDimensions());
        for (MeasureModel measureModel : model.getMeasures()) {
            this.measures.add(new EffectiveMeasure(measureModel,
                    formForest.findTree(measureModel.getFormId()),
                    dimensions));
        }
    }

    public DimensionSet getDimensions() {
        return dimensions;
    }

    public List<EffectiveMeasure> getMeasures() {
        return measures;
    }
}
