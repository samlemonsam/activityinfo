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
package org.activityinfo.store.query.shared.columns;

import org.activityinfo.store.query.shared.join.PrimaryKeyMap;

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
    private int[] keys;

    private int numRows;


    /**
     * The list of unique record ids referenced by this foreign key.
     */
    private String[] keyNames;

    public ForeignKey32(String[] keyNames, int[] keys, int numRows) {
        this.keyNames = keyNames;
        this.keys = keys;
        this.numRows = numRows;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public ForeignKey filter(int[] selectedRows) {
        int selectedKeys[] = new int[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow == -1) {
                selectedKeys[i] = -1;
            } else {
                selectedKeys[i] = keys[selectedRow];
            }
        }

        return new ForeignKey32(keyNames, selectedKeys, selectedKeys.length);
    }

    @Override
    public String getKey(int rowIndex) {
        int keyId = keys[rowIndex];
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
            int keyId = keys[i];
            if(keyId == -1) {
                mapping[i] = -1;
            } else {
                mapping[i] = rowLookup[keyId];
            }
        }
        return mapping;
    }
}
