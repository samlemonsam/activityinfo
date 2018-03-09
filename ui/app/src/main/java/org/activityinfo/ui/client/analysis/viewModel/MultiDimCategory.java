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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.BitSet;

public class MultiDimCategory {

    private String[] labels;

    @Nullable
    private BitSet bitSet;

    public MultiDimCategory(String[] labels, BitSet bitSet) {
        this.labels = labels;
        this.bitSet = bitSet;
    }

    /**
     *
     * @return true if no values belong to this intersection of categories.
     */
    public boolean isEmpty() {
        if(bitSet == null) {
            return false;
        } else {
            return bitSet.isEmpty();
        }
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    /**
     * Adds multi-valued categories to a point's group.
     *
     * @param svdCategories dimension array populated with single-valued dimension categories
     * @return a copy of svdCategories, populated with the categories from the MVDs
     */
    public String[] withMultiValuedCategories(String[] svdCategories) {
        String[] group = Arrays.copyOf(svdCategories, svdCategories.length);
        for (int i = 0; i < labels.length; i++) {
            if(labels[i] != null) {
                group[i] = labels[i];
            }
        }
        return group;
    }


    /**
     * Create a new value array that only includes values that are loaded on this
     * intersection of categories.
     */
    public double[] filter(double[] valueArray) {
        if(bitSet == null) {
            return Arrays.copyOf(valueArray, valueArray.length);

        } else {
            double filtered[] = new double[valueArray.length];
            for (int i = 0; i < valueArray.length; i++) {
                if (bitSet.get(i)) {
                    filtered[i] = valueArray[i];
                }
            }
            return filtered;
        }
    }
}
