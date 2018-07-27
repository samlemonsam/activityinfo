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
package org.activityinfo.analysis.pivot.viewModel;

import com.google.common.base.Function;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.pivot.Axis;
import org.activityinfo.model.analysis.pivot.DimensionModel;
import org.activityinfo.model.analysis.pivot.PivotModel;
import org.activityinfo.model.analysis.pivot.Statistic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class EffectiveDimension {

    private final int index;
    private final PivotModel pivotModel;
    private final DimensionModel dimensionModel;
    private final List<EffectiveMapping> effectiveMappings;
    private final String totalLabel;

    public EffectiveDimension(int index, PivotModel model, DimensionModel dimensionModel, List<EffectiveMapping> effectiveMappings) {
        this.index = index;
        this.pivotModel = model;
        this.dimensionModel = dimensionModel;
        this.effectiveMappings = effectiveMappings;
        this.totalLabel = dimensionModel.getTotalLabel().orElse(I18N.CONSTANTS.tableTotal());
    }

    public String getId() {
        return dimensionModel.getId();
    }

    public String getLabel() {
        return dimensionModel.getLabel();
    }

    public DimensionModel getModel() {
        return dimensionModel;
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
        return dimensionModel.getAxis();
    }



    public int getIndex() {
        return index;
    }

    public Function<Point, String> getCategoryProvider() {
        return (p -> p.getCategory(index));
    }

    public Comparator<String> getCategoryComparator() {
        switch (dimensionModel.getId()) {
            case DimensionModel.MEASURE_ID:
                return new CategoryComparator(pivotModel.getMeasures().stream().map(m -> m.getLabel()).collect(toList()));
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
