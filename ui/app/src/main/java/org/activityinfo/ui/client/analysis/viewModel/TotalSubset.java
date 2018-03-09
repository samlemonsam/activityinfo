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

import com.google.common.annotations.VisibleForTesting;
import org.activityinfo.ui.client.analysis.model.DimensionModel;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Defines a subset of the dimensions to use.
 */
public final class TotalSubset {

    private DimensionSet dimensionSet;
    /**
     * An array of booleans. For each dimension, {@code true} if the dimension should
     * be collapsed during this iteration.
     */
    private boolean[] totaled;
    private int collapsedCount;

    private TotalSubset(DimensionSet dimensionSet, boolean[] totaled) {
        this.dimensionSet = dimensionSet;
        this.totaled = Arrays.copyOf(totaled, totaled.length);

        // Count the number of dimensions that are being collapsed
        for (int i = 0; i < totaled.length; i++) {
            if(totaled[i]) {
                collapsedCount++;
            }
        }
    }


    /**
     * @return the number of dimensions, totaled or not.
     */
    public int getDimCount() {
        return totaled.length;
    }

    /**
     * Returns {@code true} if the given dimension should be collapsed for this
     * iteration of aggregation. The {@code dimensionIndex} refers to the index of the dimension
     * within the {@link DimensionSet}.
     */
    public boolean isDimensionCollapsed(int dimensionIndex) {
        return totaled[dimensionIndex];
    }

    public int getNumCollapsed() {
        return collapsedCount;
    }

    public int getNumUncollapsed() {
        return totaled.length - collapsedCount;
    }

    /**
     * Returns true if we should compute percentages for this combination
     * of totaled dimensions.
     */
    public boolean includePercentages() {
        if(getNumCollapsed() == 0) {
            return false;
        }
        for (int i = 0; i < dimensionSet.getCount(); i++) {
            if (isDimensionCollapsed(i)) {
                if(!dimensionSet.getDimension(i).getPercentage()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean includeTotals() {
        for (int i = 0; i < dimensionSet.getCount(); i++) {
            if (isDimensionCollapsed(i)) {
                if(!dimensionSet.getDimension(i).getTotals()) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Finds the next combination of dimensions to include in the aggregation.
     */
    @VisibleForTesting
    static boolean nextSubset(boolean[] subset, boolean[] totalsRequired) {
        // Find the right-most dimension we can "increment"
        int i = subset.length - 1;
        while(i >= 0) {
            if(!subset[i] && totalsRequired[i]) {
                subset[i] = true;
                Arrays.fill(subset, i+1, subset.length, false);
                return true;
            }
            i--;
        }
        return false;
    }

    private static boolean[] whichDimensionsRequireTotals(DimensionSet dimensionSet) {
        boolean[] totals = new boolean[dimensionSet.getCount()];
        for (int i = 0; i < dimensionSet.getCount(); i++) {
            DimensionModel dim = dimensionSet.getDimension(i);
            if(dim.getTotals() || dim.getPercentage()) {
                totals[i] = true;
            }
        }
        return totals;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < totaled.length; i++) {
            if(isDimensionCollapsed(i)) {
                sb.append("T");
            } else {
                sb.append("*");
            }
        }
        sb.append("}");
        return sb.toString();
    }


    public static Iterable<TotalSubset> set(DimensionSet dimensionSet) {

        return () -> new It(dimensionSet);

    }

    private static class It implements Iterator<TotalSubset> {

        private final DimensionSet dimensionSet;
        private final boolean totalsRequired[];
        private final boolean totaled[];
        private boolean hasNext;

        public It(DimensionSet dimensionSet) {
            this.dimensionSet = dimensionSet;
            this.totalsRequired = whichDimensionsRequireTotals(dimensionSet);
            this.totaled = new boolean[totalsRequired.length];
            this.hasNext = true;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public TotalSubset next() {
            assert hasNext;
            TotalSubset subset = new TotalSubset(dimensionSet, totaled);
            hasNext = nextSubset(totaled, totalsRequired);
            return subset;
        }
    }

}
