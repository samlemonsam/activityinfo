package org.activityinfo.ui.client.analysis.viewModel;


import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.analysis.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Set<String> definedDimensionIds = new HashSet<>();

        for (DimensionModel dimensionModel : model.getDimensions()) {
            definedDimensionIds.add(dimensionModel.getId());

            if(dimensionModel.getId().equals(DimensionModel.STATISTIC_ID)) {
                dimensions.add(new EffectiveDimension(dimensionModel));

            } else {
                int index = dimensionSet.getIndex(dimensionModel);
                List<EffectiveMapping> effectiveMappings = new ArrayList<>();
                for (EffectiveMeasure effectiveMeasure : measures) {
                    effectiveMappings.add(effectiveMeasure.getDimension(index));
                }
                dimensions.add(new EffectiveDimension(index, dimensionModel, effectiveMappings));
            }
        }

        if(model.isMeasureDefinedWithMultipleStatistics() &&
                !definedDimensionIds.contains(DimensionModel.STATISTIC_ID)) {

            dimensions.add(new EffectiveDimension(
                    ImmutableDimensionModel.builder()
                    .id(DimensionModel.STATISTIC_ID)
                    .label(I18N.CONSTANTS.statistic())
                    .build()));
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
