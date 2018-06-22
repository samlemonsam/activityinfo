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

import org.activityinfo.store.query.shared.columns.ForeignKey;
import org.activityinfo.store.spi.Slot;

import java.util.Arrays;

/**
 * Holds the foreignKey / primaryKeyMap columns required
 * to evaluate a join between a LEFT and a RIGHT table via a Reference field.
 */
public class ReferenceJoin {

    private Slot<ForeignKey> foreignKey;
    private Slot<PrimaryKeyMap> primaryKeyMap;
    private String debugName;

    private int mapping[] = null;

    public ReferenceJoin(Slot<ForeignKey> foreignKey, Slot<PrimaryKeyMap> primaryKeyMap, String debugName) {
        this.foreignKey = foreignKey;
        this.primaryKeyMap = primaryKeyMap;
        this.debugName = debugName;
    }

    /**
     *
     * @return the number of rows in the left table.
     */
    public int getLeftRowCount() {
        return foreignKey.get().numRows();
    }

    /**
     *
     * @return the foreign key(s) for each row in the LEFT table.
     */
    public ForeignKey getForeignKey() {
        return foreignKey.get();
    }

    /**
     *
     * @return mapping from the RIGHT's primary keys to the corresponding
     * row indices of the RIGHT table
     */
    public PrimaryKeyMap getPrimaryKeyMap() {
        return primaryKeyMap.get();
    }

    /**
     *
     * @return builds an array which maps each row in the left table
     * to the corresponding row in the right table.
     */
    public int[] mapping() {

        if(mapping == null) {
            ForeignKey fk = foreignKey.get();
            PrimaryKeyMap pk = primaryKeyMap.get();

            mapping = fk.buildMapping(pk);
        }

        return Arrays.copyOf(mapping, mapping.length);
    }

    public int[] copyOfMapping() {
        int[] mapping = mapping();
        return Arrays.copyOf(mapping, mapping.length);
    }

    @Override
    public String toString() {
        return debugName;
    }
}
