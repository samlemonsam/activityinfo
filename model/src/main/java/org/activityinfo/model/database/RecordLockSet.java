package org.activityinfo.model.database;

import org.activityinfo.json.JsonArrays;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.time.LocalDateInterval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RecordLockSet implements Iterable<RecordLock>, JsonSerializable {

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

    @Override
    public JsonValue toJson() {
        return JsonArrays.toJsonArray(locks);
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
