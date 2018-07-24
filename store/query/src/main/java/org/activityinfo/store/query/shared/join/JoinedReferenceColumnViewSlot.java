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
import org.activityinfo.store.spi.Slot;

import java.util.List;


/**
 * Computes a many-to-one join from a left-hand base table to a right hand table via one or more
 * reference fields.
 */
public class JoinedReferenceColumnViewSlot implements Slot<ColumnView> {

    private List<ReferenceJoin> links;
    private Slot<ColumnView> nestedColumn;

    private ColumnView result;

    public JoinedReferenceColumnViewSlot(List<ReferenceJoin> links, Slot<ColumnView> nestedColumn) {
        this.links = links;
        this.nestedColumn = nestedColumn;
    }

    @Override
    public ColumnView get() {
        if(result == null) {
            result = join();
        }
        return result;
    }

    private ColumnView join() {


        // build a vector each link that maps each row index from
        // the left table to the corresponding index in the table
        // containing our _nestedColumn_ that we want to join

        // So if LEFT is our base table, and RIGHT is the table that
        // contains _column_ that we want to join, then for each row i,
        // in the LEFT table, mapping[i] gives us the corresponding
        // row in the RIGHT table.

        int left[] = links.get(0).copyOfMapping();


        // If we have intermediate tables, we have to follow the links...

        for(int j=1;j<links.size();++j) {
            int right[] = links.get(j).mapping();
            for(int i=0;i!=left.length;++i) {
                if(left[i] != -1) {
                    left[i] = right[left[i]];
                }
            }
        }

        return nestedColumn.get().select(left);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinedReferenceColumnViewSlot that = (JoinedReferenceColumnViewSlot) o;

        if (!links.equals(that.links)) return false;
        return nestedColumn.equals(that.nestedColumn);

    }

    @Override
    public int hashCode() {
        int result = links.hashCode();
        result = 31 * result + nestedColumn.hashCode();
        return result;
    }
}
