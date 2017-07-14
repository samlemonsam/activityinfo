package org.activityinfo.store.query.shared.join;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;
import java.util.Collection;

/**
 * Mapping from ResourceId -> row index
 */
public class PrimaryKeyMap implements Serializable {

    private final Object2IntOpenHashMap<String> map;

    public PrimaryKeyMap(ColumnView id) {
        map = new Object2IntOpenHashMap<>(id.numRows());
        map.defaultReturnValue(-1);
        for (int i = 0; i < id.numRows(); i++) {
            map.put(id.getString(i), i);
        }
    }

    public int size() {
        return map.size();
    }

    /**
     *
     * Returns the row index corresponding to the given foreign key, if there
     * is exactly one foreign key, or -1 if there are multiple foreign keys
     * corresponding to primary keys or none at all.
     */
    public int getUniqueRowIndex(Collection<ResourceId> foreignKeys) {
        int matchingRowIndex = -1;
        for(ResourceId foreignKey : foreignKeys) {
            int rowIndex = map.getInt(foreignKey.asString());
            if(rowIndex != -1) {
                if(matchingRowIndex == -1) {
                    matchingRowIndex = rowIndex;
                } else {
                    // we don't do many to one in tables.
                    return -1;
                }
            }
        }
        return matchingRowIndex;
    }
    
    public int getRowIndex(String id) {
        Integer rowIndex = map.getInt(id);
        if(rowIndex == -1) {
            return -1;
        } else {
            return rowIndex;
        }
    }
    
    public int getNumRows() {
        return map.size();
    }

    public boolean contains(String parentId) {
        return map.containsKey(parentId);
    }
}
