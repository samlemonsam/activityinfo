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
package org.activityinfo.store.query.shared.join;

import org.activityinfo.store.query.shared.columns.ForeignKey;
import org.activityinfo.store.query.shared.FilterLevel;
import org.activityinfo.store.query.shared.Slot;

/**
 * Lookup key for JoinLinks in a Collection Scan
 */
public class ReferenceJoinKey {

  private final FilterLevel filterLevel;
  private final Slot<ForeignKey> foreignKey;
  private final Slot<PrimaryKeyMap> primaryKeyMap;

  public ReferenceJoinKey(FilterLevel filterLevel, Slot<ForeignKey> foreignKey, Slot<PrimaryKeyMap> primaryKeyMap) {
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
