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
package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.time.LocalDateInterval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RecordLockSet implements Iterable<RecordLock> {

    public static final RecordLockSet EMPTY = new RecordLockSet(Collections.emptyList());

    private List<RecordLock> locks;

    public RecordLockSet(List<RecordLock> locks) {
        this.locks = locks;
    }

    @Override
    public Iterator<RecordLock> iterator() {
        return locks.iterator();
    }

    public boolean isEmpty() {
        return locks.isEmpty();
    }

    public JsonValue toJson() {
        return Json.toJsonArray(locks);
    }

    public static RecordLockSet fromJson(JsonValue array) {
        List<RecordLock> locks = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            locks.add(RecordLock.fromJson(array.get(i)));
        }
        return new RecordLockSet(locks);
    }

    public boolean isLocked(LocalDateInterval period) {
        for (RecordLock lock : locks) {
            if(lock.getDateRange().overlaps(period)) {
                return true;
            }
        }
        return false;
    }
}
