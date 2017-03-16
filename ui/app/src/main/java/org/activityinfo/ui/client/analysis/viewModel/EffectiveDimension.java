package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.ui.client.analysis.model.DimensionMapping;

/**
 * Models a measure that is part of the analysis,
 * based on the user's model as well as metadata
 */
public class EffectiveDimension {
    private int index;
    private DimensionMapping mapping;

    public EffectiveDimension(int index, DimensionMapping mapping) {
        this.index = index;
        this.mapping = mapping;
    }

    public int getIndex() {
        return index;
    }

    public DimensionMapping getMapping() {
        return mapping;
    }
}
