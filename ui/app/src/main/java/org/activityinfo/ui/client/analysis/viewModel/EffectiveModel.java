package org.activityinfo.ui.client.analysis.viewModel;


import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.Axis;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;

import java.util.ArrayList;
import java.util.List;

public class EffectiveModel {

    private final DimensionSet dimensionSet;
    private final AnalysisModel model;

    private final List<EffectiveMeasure> measures = new ArrayList<>();
    private final List<EffectiveDimension> dimensions = new ArrayList<>();

    public EffectiveModel(AnalysisModel model, FormForest formForest) {
        this.model = model;
        this.dimensionSet = new DimensionSet(model.getDimensions());
        for (MeasureModel measureModel : model.getMeasures()) {
            this.measures.add(new EffectiveMeasure(measureModel,
                    formForest.findTree(measureModel.getFormId()),
                    dimensionSet));
        }

        for (int i = 0; i < dimensionSet.getCount(); i++) {
            DimensionModel dimensionModel = dimensionSet.getDimension(i);
            List<EffectiveMapping> effectiveMappings = new ArrayList<>();
            for (EffectiveMeasure effectiveMeasure : measures) {
                effectiveMappings.add(effectiveMeasure.getDimension(i));
            }
            dimensions.add(new EffectiveDimension(i, dimensionModel, effectiveMappings));
        }
    }

    public List<EffectiveDimension> getDimensions() {
        return dimensions;
    }

    public DimensionSet getDimensionSet() {
        return dimensionSet;
    }

    public List<EffectiveMeasure> getMeasures() {
        return measures;
    }

    public List<EffectiveDimension> getDimensions(Axis axis) {
        List<EffectiveDimension> items = new ArrayList<>();
        for (EffectiveDimension dimension : dimensions) {
            if(dimension.getAxis() == axis) {
                items.add(dimension);
            }
        }
        return items;
    }
}
