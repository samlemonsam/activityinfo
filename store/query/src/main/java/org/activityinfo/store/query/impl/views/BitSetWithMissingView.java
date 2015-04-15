package org.activityinfo.store.query.impl.views;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Date;

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
    public Date getDate(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        if(missing.get(row)) {
            return NA;
        } else if(missing.get(row)) {
            return TRUE;
        } else {
            return FALSE;
        }
    }
}
