package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.ui.client.analysis.model.DimensionModel;

import java.util.ArrayList;
import java.util.List;

public class DimensionListItem {

    private DimensionModel model;
    private List<EffectiveDimension> effectiveDimensions;

    public DimensionListItem(DimensionModel model, List<EffectiveDimension> effectiveDimensions) {
        this.model = model;
        this.effectiveDimensions = effectiveDimensions;
    }

    public String getId() {
        return model.id();
    }

    public String getLabel() {
        return model.label();
    }

    public DimensionModel getModel() {
        return model;
    }

    public boolean isDate() {
        for (EffectiveDimension effectiveDimension : effectiveDimensions) {
            if(effectiveDimension.isDate()) {
                return true;
            }
        }
        return false;
    }

    public static List<DimensionListItem> compute(EffectiveModel em) {
        List<DimensionListItem> listItems = new ArrayList<>();

        for (int i = 0; i < em.getDimensions().getCount(); i++) {

            DimensionModel dimension = em.getDimensions().getDimension(i);

            List<EffectiveDimension> effectiveDimensions = new ArrayList<>();
            for (EffectiveMeasure effectiveMeasure : em.getMeasures()) {
                effectiveDimensions.add(effectiveMeasure.getDimension(i));
            }

            listItems.add(new DimensionListItem(dimension, effectiveDimensions));
        }

        return listItems;
    }

}
