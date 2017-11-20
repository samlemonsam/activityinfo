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
