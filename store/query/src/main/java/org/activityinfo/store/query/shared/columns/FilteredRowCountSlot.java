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

import org.activityinfo.store.query.shared.TableFilter;
import org.activityinfo.store.spi.Slot;

public class FilteredRowCountSlot implements Slot<Integer> {
    private final Slot<Integer> countSlot;
    private final Slot<TableFilter> filterSlot;

    private Integer value;

    public FilteredRowCountSlot(Slot<Integer> countSlot, Slot<TableFilter> filterSlot) {
        this.countSlot = countSlot;
        this.filterSlot = filterSlot;
    }

    @Override
    public Integer get() {
        if(value == null) {
            TableFilter filter = filterSlot.get();
            if(filter.isAllSelected()) {
                value = countSlot.get();
            } else {
                value = filter.getBitSet().cardinality();
            }
        }
        return value;
    }
}
