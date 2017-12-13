package org.activityinfo.model.query;

import java.io.Serializable;
import java.util.BitSet;

public class BitSetWithMissingView implements ColumnView, Serializable {

    private int numRows;
    private BitSet bitSet;
    private BitSet missing;

    protected BitSetWithMissingView() {

    }

    public BitSetWithMissingView(int numRows, BitSet bitSet, BitSet missing) {
        this.numRows = numRows;
        this.bitSet = bitSet;
        this.missing = missing;
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public BitSet getMissing() {
        return missing;
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
        if(missing.get(row)) {
            return null;
        } else {
            return bitSet.get(row);
        }
    }

    @Override
    public double getDouble(int row) {
        if(missing.get(row)) {
            return Double.NaN;
        } else {
            return bitSet.get(row) ? 1d : 0d;
        }
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        if(missing.get(row)) {
            return NA;
        } else if(bitSet.get(row)) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    @Override
    public boolean isMissing(int row) {
        return missing.get(row);
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
                filteredMissing.set(i, missing.get(selectedRow));
            }
        }
        if(filteredMissing.cardinality() == 0) {
            return new BitSetColumnView(selectedRows.length, filtered);
        } else {
            return new BitSetWithMissingView(selectedRows.length, filtered, filteredMissing);
        }
    }
}
