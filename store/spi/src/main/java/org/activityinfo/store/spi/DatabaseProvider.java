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
 * Provides metadata for a database, dependent on user's assigned permissions.
 */
public interface DatabaseProvider {

  /**
   * Retrieves the UserDatabaseMeta for the given user and database.
   *
   * @param databaseId The ResourceId of the database
   * @param userId The integer id of the requesting user
   *
   * @return Optional UserDatabaseMeta
   * <ol>
   *     <li>If the database exists, and the user is the owner of the database, then the "owned" UserDatabaseMeta will
   *      be returned wrapped in an Optional</li>
   *     <li>If the database exists, and the user is assigned permission on the database, then the "granted"
   *      UserDatabaseMeta will be returned wrapped in an Optional</li>
   *     <li>If the database exists, but the user does not have an assigned permission on the database, then the
   *      "grantless" UserDatabaseMeta will be returned wrapped in an Optional</li>
   *     <li>If the database previously existed but has been deleted, then the "deleted" UserDatabaseMeta will be
   *      returned wrapped in an Optional</li>
   *     <li>If the database does not exist, then an Optional.empty() will be returned</li>
   * </ol>
   */
  Optional<UserDatabaseMeta> getDatabaseMetadata(ResourceId databaseId, int userId);

  /**
   * Retrieves the UserDatabaseMeta for the given user and database.
   *
   * @param databaseId The integer id of the database
   * @param userId The integer id of the requesting user
   *
   * @return Optional UserDatbaseMeta
   * <ol>
   *     <li>If the database exists, and the user is the owner of the database, then the "owned" UserDatabaseMeta will
   *      be returned wrapped in an Optional</li>
   *     <li>If the database exists, and the user is assigned permission on the database, then the "granted"
   *      UserDatabaseMeta will be returned wrapped in an Optional</li>
   *     <li>If the database exists, but the user does not have an assigned permission on the database, then the
   *      "grantless" UserDatabaseMeta will be returned wrapped in an Optional</li>
   *     <li>If the database previously existed but has been deleted, then the "deleted" UserDatabaseMeta will be
   *      returned wrapped in an Optional</li>
   *     <li>If the database does not exist, then an Optional.empty() will be returned</li>
   * </ol>
   */
  Optional<UserDatabaseMeta> getDatabaseMetadata(int databaseId, int userId);

  /**
   * Retrieves the UserDatabaseMeta for the given user on a set of databases.
   *
   * @param databaseIds The set of database ResourceIds
   * @param userId The integer id of the user
   *
   * @return Map of ResourceId -> UserDatabaseMeta
   * <ol>
   *     <li>If the database exists, and the user is the owner of the database, then the "owned" UserDatabaseMeta will
   *      be returned in the map</li>
   *     <li>If the database exists, and the user is assigned permission on the database, then the "granted"
   *      UserDatabaseMeta will be returned in the map</li>
   *     <li>If the database exists, but the user does not have an assigned permission on the database, then the
   *      "grantless" UserDatabaseMeta will be returned in the map</li>
   *     <li>If the database previously existed but has been deleted, then the "deleted" UserDatabaseMeta will be
   *      returned in the map</li>
   *     <li>If the database does not exist, then it will <b>not</b> be returned in the map</li>
   * </ol>
   */
  Map<ResourceId,UserDatabaseMeta> getDatabaseMetadata(Set<ResourceId> databaseIds, int userId);

  /**
   * Retrieves all UserDatabaseMeta visible to the given user. This includes owned and assigned databases.
   *
   * @param userId The integer id of the user
   *
   * @return List of visible UserDatabaseMeta
   * <ol>
   *     <li>If user is the owner of the database, then the "owned" UserDatabaseMeta will be returned in the list</li>
   *     <li>If the user is assigned permission on the database, then the "granted" UserDatabaseMeta will be returned
   *      in the list</li>
   * </ol>
   */
  List<UserDatabaseMeta> getVisibleDatabases(int userId);

  /**
   * Retrieves the UserDatabaseMeta for the given user which contains the given resource.
   *
   * @param resourceId The ResourceId of the resource
   * @param userId The integer id of the requesting user
   *
   * @return Optional UserDatabaseMeta
   * <ol>
   *     <li>If the resource's database exists, and the user is the owner of the database, then the "owned"
   *      UserDatabaseMeta will be returned wrapped in an Optional</li>
   *     <li>If the resource's database exists, and the user is assigned permission on the database, then the
   *      "granted" UserDatabaseMeta will be returned wrapped in an Optional</li>
   *     <li>If the resource's database exists, but the user does not have an assigned permission on the database,
   *      then the "grantless" UserDatabaseMeta will be returned wrapped in an Optional</li>
   *     <li>If the resource's database previously existed but has been deleted, then the "deleted" UserDatabaseMeta
   *      will be returned wrapped in an Optional</li>
   *     <li>If the database does not exist or cannot be found for the given resource, then an Optional.empty() will
   *      be returned</li>
   * </ol>
   */
  Optional<UserDatabaseMeta> getDatabaseMetadataByResource(ResourceId resourceId, int userId);

}
