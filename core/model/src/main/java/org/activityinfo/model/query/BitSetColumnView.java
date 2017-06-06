package org.activityinfo.model.query;

import java.io.Serializable;
import java.util.BitSet;

/**
 * A {@code ColumnView} of a boolean collection field that uses
 * a {@link java.util.BitSet} for storage. The field must have no
 * missing values.
 */
public class BitSetColumnView implements ColumnView, Serializable {

    private int numRows;
    private BitSet bitSet;

    protected BitSetColumnView() {}

    public BitSetColumnView(int numRows, BitSet bitSet) {
        this.numRows = numRows;
        this.bitSet = bitSet;
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.BOOLEAN;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public Object get(int row) {
        return bitSet.get(row);
    }

    @Override
    public double getDouble(int row) {
        return bitSet.get(row) ? 1d : 0d;
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return bitSet.get(row) ? TRUE : FALSE;
    }

    @Override
    public boolean isMissing(int row) {
        return false;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        BitSet filtered = new BitSet();
        BitSet filteredMissing = new BitSet();
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow == -1) {
                filteredMissing.set(i);
            } else {
                filtered.set(i, bitSet.get(selectedRow));
            }
        }
        if(filteredMissing.isEmpty()) {
            return new BitSetColumnView(selectedRows.length, filtered);
        } else {
            return new BitSetWithMissingView(selectedRows.length, filtered, filteredMissing);
        }
    }

}
