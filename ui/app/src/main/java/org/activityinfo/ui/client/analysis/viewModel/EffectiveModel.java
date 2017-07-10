package org.activityinfo.ui.client.analysis.viewModel;


import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.analysis.model.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EffectiveModel {

    private final DimensionSet dimensionSet;
    private final AnalysisModel model;

    private final List<EffectiveMeasure> measures = new ArrayList<>();
    private final List<EffectiveDimension> dimensions = new ArrayList<>();

    public EffectiveModel(AnalysisModel model, FormForest formForest) {
        this.model = model;

        List<DimensionModel> dimensions = new ArrayList<>(model.getDimensions());

        // If we have multiple measures, than they MUST be labled with a measure dimension
        if(model.getMeasures().size() > 1 &&
            !isDefined(dimensions, DimensionModel.MEASURE_ID)) {
            dimensions.add(
                ImmutableDimensionModel.builder()
                    .id(DimensionModel.MEASURE_ID)
                    .label(I18N.CONSTANTS.measures())
                    .build());
        }

        // Include a Statistics dimension if required but not added by
        // the user to the model.
        if(model.isMeasureDefinedWithMultipleStatistics() &&
                !isDefined(dimensions, DimensionModel.STATISTIC_ID)) {

            dimensions.add(
                    ImmutableDimensionModel.builder()
                            .id(DimensionModel.STATISTIC_ID)
                            .label(I18N.CONSTANTS.statistic())
                            .build());
        }

        // Create a DimensionSet, which maps each dimension
        // to an integer index.
        this.dimensionSet = new DimensionSet(dimensions);

        for (MeasureModel measureModel : model.getMeasures()) {
            this.measures.add(new EffectiveMeasure(measureModel,
                    formForest.findTree(measureModel.getFormId()),
                    dimensionSet));
        }

        for (DimensionModel dimensionModel : dimensions) {

            int index = dimensionSet.getIndex(dimensionModel);
            List<EffectiveMapping> effectiveMappings = new ArrayList<>();
            for (EffectiveMeasure effectiveMeasure : measures) {
                effectiveMappings.add(effectiveMeasure.getDimension(index));
            }
            this.dimensions.add(new EffectiveDimension(index, model, dimensionModel, effectiveMappings));
        }
    }

    public AnalysisModel getModel() {
        return model;
    }

    private boolean isDefined(List<DimensionModel> dimensions, String dimensionId) {
        for (DimensionModel dimension : dimensions) {
            if(dimension.getId().equals(dimensionId)) {
                return true;
            }
        }
        return false;
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
