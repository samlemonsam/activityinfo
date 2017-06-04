package org.activityinfo.store.query.impl.join;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Maps row indices to reference values for a given field.
 */
public class ForeignKeyMap implements Serializable {

    public static final ForeignKeyMap EMPTY =
            new ForeignKeyMap(0, Multimaps.forMap(Collections.<Integer, ResourceId>emptyMap()));

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

    public ForeignKeyMap filter(BitSet bitSet) {
        Multimap<Integer, ResourceId> filteredKeys = HashMultimap.create(this.keys);
        for (Map.Entry<Integer, ResourceId> entry : keys.entries()) {
            int rowIndex = entry.getKey();
            if(bitSet.get(rowIndex)) {
                filteredKeys.put(rowIndex, entry.getValue());
            }
        }
        int numRows = bitSet.cardinality();

        return new ForeignKeyMap(numRows, filteredKeys);
    }
}
