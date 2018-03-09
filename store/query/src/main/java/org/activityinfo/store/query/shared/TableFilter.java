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
package org.activityinfo.store.query.shared;

import com.google.common.base.Preconditions;
import org.activityinfo.model.query.BitSetColumnView;
import org.activityinfo.model.query.BitSetWithMissingView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EmptyColumnView;
import org.activityinfo.store.query.shared.columns.ForeignKey;

import java.util.BitSet;

/**
 * Stores a row-based filter for a column set.
 *
 */
public class TableFilter {

    public static final TableFilter ALL_SELECTED = new TableFilter();

    public static final TableFilter NONE_SELECTED = new TableFilter(new BitSet());

    private BitSet bitSet;

    private int[] selectedRows = null;

    private TableFilter() {
    }

    public TableFilter(ColumnView view) {
        if(view instanceof BitSetColumnView) {
            this.bitSet = ((BitSetColumnView) view).getBitSet();
        } else if(view instanceof BitSetWithMissingView) {
            this.bitSet = ((BitSetWithMissingView) view).getBitSet();
        } else {
            this.bitSet = new BitSet();
            for (int i = 0; i < view.numRows(); i++) {
                bitSet.set(i, view.getBoolean(i) == ColumnView.TRUE);
            }
        }
    }

    public TableFilter(BitSet bitSet) {
        this.bitSet = bitSet;
    }

    public boolean isAllSelected() {
        return bitSet == null;
    }

    public BitSet getBitSet() {
        Preconditions.checkNotNull(bitSet);
        return bitSet;
    }

    private int[] getSelectedRows() {
        if(selectedRows == null) {
            selectedRows = new int[bitSet.cardinality()];
            int rowIndex = 0;
            for (int i = 0; i < bitSet.size(); i++) {
                if(bitSet.get(i)) {
                    selectedRows[rowIndex++] = i;
                }
            }
        }
        return selectedRows;
    }

    public TableFilter intersection(TableFilter other) {
        if(isAllSelected()) {
            return other;
        } else if(other.isAllSelected()) {
            return this;
        } else {
            BitSet intersection = (BitSet)this.bitSet.clone();
            intersection.and(other.bitSet);

            return new TableFilter(intersection);
        }
    }

    public ColumnView apply(ColumnView columnView) {
        if(isAllSelected()) {
            return columnView;
        } else if(bitSet.isEmpty()) {
            return new EmptyColumnView(columnView.getType(), 0);
        } else {
            return columnView.select(getSelectedRows());
        }
    }

    public ForeignKey apply(ForeignKey foreignKey) {
        if(isAllSelected()) {
            return foreignKey;
        } else {
            return foreignKey.filter(getSelectedRows());
        }
    }

    @Override
    public String toString() {
        if(isAllSelected()) {
            return "TableFilter{ALL}";
        } else if(bitSet.isEmpty()) {
            return "TableFilter{NONE}";
        } else {
            return "TableFilter{" + bitSet.cardinality() + "}";
        }
    }
}
