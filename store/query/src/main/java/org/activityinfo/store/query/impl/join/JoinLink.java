package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.impl.Slot;

import java.util.Collection;

/**
 * Holds the foreignKey / primaryKeyMap columns required
 * to evaluate a left join between a LEFT and a RIGHT table.
 */
public class JoinLink {
    private Slot<ForeignKeyMap> foreignKey;
    private Slot<PrimaryKeyMap> primaryKeyMap;

    public JoinLink(Slot<ForeignKeyMap> foreignKey, Slot<PrimaryKeyMap> primaryKeyMap) {
        this.foreignKey = foreignKey;
        this.primaryKeyMap = primaryKeyMap;
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
    public int[] buildMapping() {
        ForeignKeyMap fk = foreignKey.get();
        PrimaryKeyMap pk = primaryKeyMap.get();

        int mapping[] = new int[fk.getNumRows()];
        for(int i=0;i!=mapping.length;++i) {
            Collection<ResourceId> foreignKeys = fk.getKeys(i);
            mapping[i] = pk.getUniqueRowIndex(foreignKeys);
        }
        return mapping;
    }
}
