package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.impl.Slot;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Holds the foreignKey / primaryKeyMap columns required
 * to evaluate a join between a LEFT and a RIGHT table via a Reference field.
 */
public class ReferenceJoin {

    private Slot<ForeignKeyMap> foreignKey;
    private Slot<PrimaryKeyMap> primaryKeyMap;
    private String debugName;

    private int mapping[] = null;

    public ReferenceJoin(Slot<ForeignKeyMap> foreignKey, Slot<PrimaryKeyMap> primaryKeyMap, String debugName) {
        this.foreignKey = foreignKey;
        this.primaryKeyMap = primaryKeyMap;
        this.debugName = debugName;
    }

    /**
     *
     * @return the number of rows in the left table.
     */
    public int getLeftRowCount() {
        return foreignKey.get().getNumRows();
    }

    /**
     *
     * @return the foreign key(s) for each row in the LEFT table.
     */
    public ForeignKeyMap getForeignKey() {
        return foreignKey.get();
    }

    /**
     *
     * @return mapping from the RIGHT's primary keys to the corresponding
     * row indices of the RIGHT table
     */
    public PrimaryKeyMap getPrimaryKeyMap() {
        return primaryKeyMap.get();
    }

    /**
     *
     * @return builds an array which maps each row in the left table
     * to the corresponding row in the right table.
     */
    public int[] mapping() {

        if(mapping == null) {
            ForeignKeyMap fk = foreignKey.get();
            PrimaryKeyMap pk = primaryKeyMap.get();

            mapping = new int[fk.getNumRows()];
            for (int i = 0; i != mapping.length; ++i) {
                Collection<ResourceId> foreignKeys = fk.getKeys(i);
                mapping[i] = pk.getUniqueRowIndex(foreignKeys);
            }
        }

        return Arrays.copyOf(mapping, mapping.length);
    }

    public int[] copyOfMapping() {
        int[] mapping = mapping();
        return Arrays.copyOf(mapping, mapping.length);
    }

    @Override
    public String toString() {
        return debugName;
    }
}
