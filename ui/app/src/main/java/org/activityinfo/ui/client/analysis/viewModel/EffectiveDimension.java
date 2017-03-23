package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import org.activityinfo.ui.client.analysis.model.Axis;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.Statistic;

import java.util.Collections;
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

    public EffectiveDimension(DimensionModel model) {
        this.model = model;
        this.effectiveMappings = Collections.emptyList();
        this.index = -1;
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



    public int getIndex() {
        return index;
    }

    public Function<Point, String> getCategoryProvider() {
        switch (model.getId()) {
            case DimensionModel.STATISTIC_ID:
                return (p -> p.getStatistic().getLabel());

            default:
                return (p -> p.getCategory(index));
        }
    }

    public Comparator<String> getCategoryComparator() {
        switch (model.getId()) {
            case DimensionModel.STATISTIC_ID:
                return Ordering.explicit(Statistic.labels());
            default:
                return Ordering.natural();
        }
    }


}
