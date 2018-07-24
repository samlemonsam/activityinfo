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
package org.activityinfo.store.query.shared.join;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.shared.TableFilter;
import org.activityinfo.store.spi.Slot;

import java.util.BitSet;


public class ParentMask implements Slot<TableFilter> {

    private final Slot<PrimaryKeyMap> parentKeySlot;
    private final Slot<ColumnView> parentIdSlot;

    private TableFilter result = null;

    public ParentMask(Slot<PrimaryKeyMap> parentKeySlot, Slot<ColumnView> parentIdSlot) {
        this.parentKeySlot = parentKeySlot;
        this.parentIdSlot = parentIdSlot;
    }

    @Override
    public TableFilter get() {
        if(result == null) {
            BitSet bitSet = new BitSet();
            PrimaryKeyMap parentKeyMap = parentKeySlot.get();
            ColumnView parentView = parentIdSlot.get();

            for (int i = 0; i < parentView.numRows(); i++) {
                String parentId = parentView.getString(i);
                bitSet.set(i, parentKeyMap.contains(parentId));
            }
            result = new TableFilter(bitSet);
        }
        return result;
    }
}
