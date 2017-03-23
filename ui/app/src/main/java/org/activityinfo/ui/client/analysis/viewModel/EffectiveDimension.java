package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.collect.Ordering;
import org.activityinfo.ui.client.analysis.model.Axis;
import org.activityinfo.ui.client.analysis.model.DimensionModel;

import java.util.Comparator;
import java.util.List;

public class EffectiveDimension {

    private int index;
    private DimensionModel model;
    private List<EffectiveMapping> effectiveMappings;

    public EffectiveDimension(int index, DimensionModel model, List<EffectiveMapping> effectiveMappings) {
        this.index = index;
        this.model = model;
        this.effectiveMappings = effectiveMappings;
    }

    public String getId() {
        return model.getId();
    }

    public String getLabel() {
        return model.getLabel();
    }

    public DimensionModel getModel() {
        return model;
    }

    public boolean isDate() {
        for (EffectiveMapping effectiveMapping : effectiveMappings) {
            if(effectiveMapping.isDate()) {
                return true;
            }
        }
        return false;
    }

    public Axis getAxis() {
        return model.getAxis();
    }


    public Comparator<String> getCategoryComparator() {
        return Ordering.natural();
    }

    public int getIndex() {
        return index;
    }

}
