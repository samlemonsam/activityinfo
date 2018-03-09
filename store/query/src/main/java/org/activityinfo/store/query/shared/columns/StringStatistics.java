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

import com.google.common.collect.Sets;

import java.util.Set;

public class StringStatistics {

    public static final int UNIQUE_VALUE_LIMIT = 50;

    private int count;
    private int missingCount;

    private Set<Object> unique = Sets.newHashSet();

    /**
     * Stop tracking unique values after we hit 50 values
     */
    private boolean uniquish = true;

    public void update(Object value) {
        count++;
        if(value == null) {
            missingCount ++;
        } else {
            if(uniquish) {
                unique.add(value);
                if(unique.size() > UNIQUE_VALUE_LIMIT) {
                    uniquish = false;
                }
            }
        }
    }

    public boolean isEmpty() {
        return missingCount == count;
    }

    public boolean isConstant() {
        return unique.size() == 1 && missingCount == 0;
    }

}
