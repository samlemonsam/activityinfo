package org.activityinfo.ui.client.analysis.viewModel;

import java.util.Arrays;
import java.util.BitSet;

public class MultiDimCategory {

    private String[] labels;
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
        return bitSet.isEmpty();
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    /**
     *
     * @param svdCategories dimension array populated with single-valued dimension categories
     * @return a copy of svdCategories, populated with the categories from the MVDs
     */
    public String[] group(String[] svdCategories) {
        String[] group = Arrays.copyOf(svdCategories, svdCategories.length);
        for (int i = 0; i < labels.length; i++) {
            if(labels[i] != null) {
                group[i] = labels[i];
            }
        }
        return group;
    }
}
