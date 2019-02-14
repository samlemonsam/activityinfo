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
package org.activityinfo.store.spi;

import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Provides metadata for a database, dependent on user's permissions.
 */
public interface DatabaseProvider {

  Optional<UserDatabaseMeta> getDatabaseMetadata(ResourceId databaseId, int userId);

  Optional<UserDatabaseMeta> getDatabaseMetadata(int databaseId, int userId);

  Map<ResourceId,UserDatabaseMeta> getDatabaseMetadata(Set<ResourceId> databaseIds, int userId);

  List<UserDatabaseMeta> getVisibleDatabases(int userId);

  Optional<UserDatabaseMeta> getDatabaseMetadataByResource(ResourceId resourceId, int userId);

}
