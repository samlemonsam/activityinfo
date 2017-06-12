package org.activityinfo.model.query;

import java.util.BitSet;

/**
 * Default implementations of ColumnView methods.
 */
public class ColumnViewDefaults {


    /**
     * Default implementation of the toBitSet view.
     */
    public static BitSet toBitSet(ColumnView view) {
        BitSet bitSet = new BitSet();
        for (int i = 0; i < view.numRows(); i++) {
            if(view.getBoolean(i) == ColumnView.TRUE) {
                bitSet.set(i, true);
            }
        }
        return bitSet;
    }
}
