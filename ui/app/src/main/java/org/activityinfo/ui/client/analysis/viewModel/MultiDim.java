package org.activityinfo.ui.client.analysis.viewModel;

import java.util.BitSet;

class MultiDim {
    private int index;
    private String[] labels;
    private final BitSet[] bitSets;

    MultiDim(int dimensionIndex, String[] labels, BitSet[] bitSets) {
        this.index = dimensionIndex;
        this.labels = labels;
        this.bitSets = bitSets;
    }

    int getCategoryCount() {
        return labels.length;
    }

    public BitSet getBitSet(int categoryIndex) {
        return bitSets[categoryIndex];
    }

    public int getDimensionIndex() {
        return index;
    }

    public String getLabel(int categoryIndex) {
        return labels[categoryIndex];
    }

    public int countExpandedRows() {
        int count = 0;
        for (BitSet bitSet : bitSets) {
            count += bitSet.cardinality();
        }
        return count;
    }

}
