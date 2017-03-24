package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.base.Function;
import org.activityinfo.i18n.shared.I18N;
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
    private String totalLabel;

    public EffectiveDimension(int index, DimensionModel model, List<EffectiveMapping> effectiveMappings) {
        this.index = index;
        this.model = model;
        this.effectiveMappings = effectiveMappings;
        this.totalLabel = model.getTotalLabel().orElse(I18N.CONSTANTS.tableTotal());
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

    public String getTotalLabel() {
        return totalLabel;
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
        return (p -> p.getCategory(index));
    }

    public Comparator<String> getCategoryComparator() {
        switch (model.getId()) {
            case DimensionModel.STATISTIC_ID:
                return new CategoryComparator(Statistic.labels());
            default:
                return new CategoryComparator();
        }
    }

    private static class CategoryComparator implements Comparator<String> {

        private final List<String> explicitOrder;

        public CategoryComparator(List<String> explicitOrder) {
            this.explicitOrder = explicitOrder;
        }

        public CategoryComparator() {
            this.explicitOrder = Collections.emptyList();
        }

        @Override
        public int compare(String a, String b) {

            // FIRST: Total categories always are sorted to the end

            boolean ta = Point.TOTAL.equals(a);
            boolean tb = Point.TOTAL.equals(b);
            if(ta && tb) {
                return 0;
            } else if(ta) {
                return +1;
            } else if(tb) {
                return -1;
            }

            // THEN: take into account any explicit ordering provided.
            // Explicitly ordered values always proceed unordered values.
            if(!explicitOrder.isEmpty()) {
                int ia = explicitOrder.indexOf(a);
                int ib = explicitOrder.indexOf(b);

                if (ia != -1 && ib != -1) {
                    // both values are explicitly ordered
                    return Integer.compare(ia, ib);
                }
                if (ib != -1) {
                    // a is not explicitly ordered, so follows b
                    return +1;
                }
                if (ia != -1) {
                    // b is not explicity ordered, so follows a
                    return +1;
                }
            }

            // Finally, if neither values are explicitly ordered, use natural (alphabetic) order
            return a.compareTo(b);
        }
    }

}
