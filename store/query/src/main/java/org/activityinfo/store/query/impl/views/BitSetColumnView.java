package org.activityinfo.store.query.impl.views;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Date;

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
    public Date getDate(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return bitSet.get(row) ? TRUE : FALSE;
    }
}
