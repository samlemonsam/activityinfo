package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Mapping from ResourceId -> row index
 */
public class PrimaryKeyMap implements Serializable {

    private final Map<ResourceId, Integer> map;

    public PrimaryKeyMap(Map<ResourceId, Integer> map) {
        this.map = map;
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
            Integer rowIndex = map.get(foreignKey);
            if(rowIndex != null) {
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
}
