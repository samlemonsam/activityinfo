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

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.model.DimensionMapping;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a measure that is part of the analysis,
 * based on the user's model as well as metadata
 */
public class EffectiveMeasure {
    private MeasureModel model;
    private FormTree formTree;
    private DimensionSet dimensionSet;

    private List<EffectiveMapping> dimensions = new ArrayList<>();

    public EffectiveMeasure(MeasureModel model, FormTree formTree, DimensionSet dimensionSet) {
        this.model = model;
        this.formTree = formTree;
        this.dimensionSet = dimensionSet;

        for (int i = 0; i < dimensionSet.getCount(); i++) {
            DimensionModel dimension = dimensionSet.getDimension(i);
            DimensionMapping mapping = findMapping(model, dimension);
            dimensions.add(new EffectiveMapping(formTree, i, dimension, mapping));
        }
    }

    public ResourceId getFormId() {
        return formTree.getRootFormId();
    }

    public MeasureModel getModel() {
        return model;
    }

    private static DimensionMapping findMapping(MeasureModel measure, DimensionModel dimension) {

        // First try to find a mapping specifically for this form
        for (DimensionMapping mapping : dimension.getMappings()) {
            if(mapping.getFormId() != null &&
               mapping.getFormId().equals(measure.getFormId())) {
                return mapping;
            }
        }

        // If nothing matches, try a free floating formula...
        for (DimensionMapping mapping : dimension.getMappings()) {
            if(mapping.getFormId() == null) {
                return mapping;
            }
        }
        return null;
    }

    public DimensionSet getDimensionSet() {
        return dimensionSet;
    }

    public List<EffectiveMapping> getDimensions() {
        return dimensions;
    }

    public EffectiveMapping getDimension(int i) {
        return dimensions.get(i);
    }

    public void setDimensionSet(DimensionSet dimensionSet) {
        this.dimensionSet = dimensionSet;
    }
}

