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
