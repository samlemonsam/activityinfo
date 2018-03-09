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
package org.activityinfo.store.query.client.join;

import com.google.gwt.core.client.JavaScriptObject;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.shared.join.PrimaryKeyMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple PrimaryKeyMap that can be compiled to JavaScript
 */
public class SimplePrimaryKeyMap implements PrimaryKeyMap {

    private Map<String, Integer> map = new HashMap<>();

    public SimplePrimaryKeyMap(ColumnView idColumn) {
        for (int i = 0; i < idColumn.numRows(); i++) {
            map.put(idColumn.getString(i), i);
        }
    }

    @Override
    public int numRows() {
        return map.size();
    }

    @Override
    public int getRowIndex(String recordId) {
        Integer rowIndex = map.get(recordId);
        if(rowIndex == null) {
            return -1;
        }
        return rowIndex;
    }

    @Override
    public boolean contains(String recordId) {
        return map.containsKey(recordId);
    }
}
