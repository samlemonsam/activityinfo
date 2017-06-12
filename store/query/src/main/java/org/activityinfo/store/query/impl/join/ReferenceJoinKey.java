package org.activityinfo.store.query.impl.join;

import org.activityinfo.store.query.impl.FilterLevel;
import org.activityinfo.store.query.impl.Slot;

/**
 * Lookup key for JoinLinks in a Collection Scan
 */
public class ReferenceJoinKey {

  private final FilterLevel filterLevel;
  private final Slot<ForeignKeyMap> foreignKey;
  private final Slot<PrimaryKeyMap> primaryKeyMap;

  public ReferenceJoinKey(FilterLevel filterLevel, Slot<ForeignKeyMap> foreignKey, Slot<PrimaryKeyMap> primaryKeyMap) {
    this.filterLevel = filterLevel;
    this.foreignKey = foreignKey;
    this.primaryKeyMap = primaryKeyMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ReferenceJoinKey that = (ReferenceJoinKey) o;

    if (filterLevel != that.filterLevel) return false;
    if (!foreignKey.equals(that.foreignKey)) return false;
    return primaryKeyMap.equals(that.primaryKeyMap);

  }

  @Override
  public int hashCode() {
    int result = filterLevel.hashCode();
    result = 31 * result + foreignKey.hashCode();
    result = 31 * result + primaryKeyMap.hashCode();
    return result;
  }
}
