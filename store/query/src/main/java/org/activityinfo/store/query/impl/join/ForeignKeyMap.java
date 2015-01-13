package org.activityinfo.store.query.impl.join;

import com.google.common.collect.Multimap;
import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;
import java.util.Collection;

/**
 * Maps row indices to reference values for a given field.
 */
public class ForeignKeyMap implements Serializable {

    /**
     * Maps row index to ResourceId of related entity
     */
    private Multimap<Integer, ResourceId> keys;
    private int numRows;

    public ForeignKeyMap(int numRows, Multimap<Integer, ResourceId> keys) {
        this.numRows = numRows;
        this.keys = keys;
    }

    public int getNumRows() {
        return numRows;
    }

    public Collection<ResourceId> getKeys(int rowIndex) {
        return keys.get(rowIndex);
    }
}
