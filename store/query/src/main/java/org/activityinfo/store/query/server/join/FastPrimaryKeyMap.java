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
package org.activityinfo.store.query.server.join;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.shared.join.PrimaryKeyMap;

import java.io.Serializable;

/**
 * Mapping from ResourceId -> row index
 */
public class FastPrimaryKeyMap implements Serializable, PrimaryKeyMap {

    private final Object2IntOpenHashMap<String> map;

    public FastPrimaryKeyMap(ColumnView id) {
        map = new Object2IntOpenHashMap<>(id.numRows());
        map.defaultReturnValue(-1);
        for (int i = 0; i < id.numRows(); i++) {
            map.put(id.getString(i), i);
        }
    }

    @Override
    public int getRowIndex(String recordId) {
        Integer rowIndex = map.getInt(recordId);
        if(rowIndex == -1) {
            return -1;
        } else {
            return rowIndex;
        }
    }
    
    @Override
    public int numRows() {
        return map.size();
    }

    @Override
    public boolean contains(String recordId) {
        return map.containsKey(recordId);
    }
}
