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

    private List<EffectiveDimension> dimensions = new ArrayList<>();

    public EffectiveMeasure(MeasureModel model, FormTree formTree, DimensionSet dimensionSet) {
        this.model = model;
        this.formTree = formTree;
        this.dimensionSet = dimensionSet;

        for (int i = 0; i < dimensionSet.getCount(); i++) {
            DimensionModel dimension = dimensionSet.getDimension(i);
            DimensionMapping mapping = findMapping(model, dimension);
            dimensions.add(new EffectiveDimension(formTree, i, dimension, mapping));
        }
    }

    public ResourceId getFormId() {
        return formTree.getRootFormClass().getId();
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

    public List<EffectiveDimension> getDimensions() {
        return dimensions;
    }

    public EffectiveDimension getDimension(int i) {
        return dimensions.get(i);
    }

    public void setDimensionSet(DimensionSet dimensionSet) {
        this.dimensionSet = dimensionSet;
    }
}

