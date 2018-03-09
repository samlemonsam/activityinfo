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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Set of multi-valued dimensions
 */
public class MultiDimSet {

    private List<MultiDim> dims = new ArrayList<>();
    private int totalDimCount;

    public MultiDimSet(List<MultiDim> dims) {
        this.dims = dims;

        for (MultiDim dim : dims) {
            if(dim.getDimensionIndex()+1 > totalDimCount) {
                totalDimCount = dim.getDimensionIndex()+1;
            }
        }
    }

    public boolean isEmpty() {
        return dims.isEmpty();
    }


    public MultiDimSet regroup(TotalSubset totalSubset) {
        List<MultiDim> included = new ArrayList<>();
        for (MultiDim dim : dims) {
            if(!totalSubset.isDimensionCollapsed(dim.getDimensionIndex())) {
                included.add(dim);
            }
        }
        return new MultiDimSet(included);
    }


    public List<MultiDimCategory> build() {
        /*
         * When we have multiple, multi-valued dimensions,
         * we need the cross product of each set of dimension categories.
         *
         * For example, if you have three MVDs, A, B, C with categories:
         *
         * A = [ A1, A2, A3 ]
         * B = [ B1, B2 ]
         * C = [ C1, C2 ]
         *
         * Then we need...
         * A1*B1*C1
         * A1*B1*C2
         * A1*B2*C1
         * A1*B2*C2
         * A2*B1*C1
         * ....
         */

        List<MultiDimCategory> list = new ArrayList<>();

        int categories[] = new int[dims.size()];
        do {
            MultiDimCategory intersection = intersect(categories);
            if (!intersection.isEmpty()) {
                list.add(intersection);
            }
        } while(nextCombination(categories));

        return list;
    }


    boolean nextCombination(int[] categories) {
        // find right most category to increment
        int i = dims.size()- 1;
        while(i >= 0) {
            if(categories[i]+1 < dims.get(i).getCategoryCount()) {
                categories[i]++;
                Arrays.fill(categories, i+1, categories.length, 0);
                return true;
            }
            i--;
        }
        return false;
    }

    private MultiDimCategory intersect(int[] categories) {
        BitSet bitSet = null;
        String labels[] = new String[totalDimCount];
        for (int i = 0; i < dims.size(); i++) {
            MultiDim dim = dims.get(i);
            int categoryIndex = categories[i];

            labels[dim.getDimensionIndex()] = dim.getLabel(categoryIndex);
            BitSet categoryBitSet = dim.getBitSet(categoryIndex);
            if(bitSet == null) {
                bitSet = (BitSet) categoryBitSet.clone();
            } else {
                bitSet.and(categoryBitSet);
            }
        }
        return new MultiDimCategory(labels, bitSet);
    }
}
