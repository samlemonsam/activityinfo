/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.analysis.model.*;

import java.util.ArrayList;
import java.util.List;

public class EffectiveModel {

    private final DimensionSet dimensionSet;
    private final PivotModel model;

    private final List<EffectiveMeasure> measures = new ArrayList<>();
    private final List<EffectiveDimension> dimensions = new ArrayList<>();

    public EffectiveModel(PivotModel model, FormForest formForest) {
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

    public PivotModel getModel() {
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
