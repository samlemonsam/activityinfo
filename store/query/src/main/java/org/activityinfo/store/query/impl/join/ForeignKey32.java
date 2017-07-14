package org.activityinfo.store.query.impl.join;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.Serializable;

/**
 * Maps row indices to reference values for a given field.
 */
public class ForeignKey32 implements Serializable, ForeignKey {


    /**
     * The foreign keys for each row in the left-hand table.
     *
     * keys[i] provides the integer key id for the i-th row of the
     * left hand table.
     *
     * keyNames[key[i]] provides the String record key for the i-th row
     * of the left-hand table.
     */
    private IntArrayList keys;


    /**
     * The list of unique record ids referenced by this foreign key.
     */
    private String[] keyNames;

    public static ForeignKey32 zeroRows() {
        return new ForeignKey32(new String[0], new IntArrayList(0));
    }

    public ForeignKey32(String[] keyNames, IntArrayList keys) {
        this.keyNames = keyNames;
        this.keys = keys;
    }

    @Override
    public int numRows() {
        return keys.size();
    }

    @Override
    public ForeignKey filter(int[] selectedRows) {
        IntArrayList selectedKeys = new IntArrayList(selectedRows.length);
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow == -1) {
                selectedKeys.add(-1);
            } else {
                selectedKeys.add(keys.getInt(selectedRow));
            }
        }

        assert selectedKeys.size() == selectedRows.length;

        return new ForeignKey32(keyNames, selectedKeys);
    }

    @Override
    public String getKey(int rowIndex) {
        int keyId = keys.getInt(rowIndex);
        if(keyId == -1) {
            return null;
        } else {
            return keyNames[keyId];
        }
    }

    /**
     * Builds an array which maps each foreign key
     * to the corresponding row in the right table, indexed by the given PrimaryKey.
     */
    @Override
    public int[] buildMapping(PrimaryKeyMap pk) {

        // First map the our key Names to rows in the right table
        int rowLookup[] = new int[keyNames.length];
        for (int i = 0; i < keyNames.length; i++) {
            rowLookup[i] = pk.getRowIndex(keyNames[i]);
        }

        // Now we can use this lookup to find the row indexes
        // for all the rows in the left table.
        int mapping[] = new int[numRows()];
        for (int i = 0; i < mapping.length; i++) {
            int keyId = keys.getInt(i);
            if(keyId == -1) {
                mapping[i] = -1;
            } else {
                mapping[i] = rowLookup[keyId];
            }
        }
        return mapping;
    }
}
