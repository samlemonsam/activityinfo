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
package org.activityinfo.store.testing;

import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.UserDatabaseProvider;

import java.util.*;
import java.util.stream.Collectors;

public class TestingUserDatabaseProvider implements UserDatabaseProvider {

    private Map<ResourceId, UserDatabaseMeta> databaseMap = new HashMap<>();
    private Map<ResourceId, UserDatabaseMeta> resourceMap = new HashMap<>();

    public TestingUserDatabaseProvider() {
    }

    public void add(UserDatabaseMeta database) {
        databaseMap.put(database.getDatabaseId(), database);
        resourceMap.put(database.getDatabaseId(), database);
        for (Resource resource : database.getResources()) {
            resourceMap.put(resource.getId(), database);
        }
    }

    public Optional<UserDatabaseMeta> lookupDatabase(ResourceId resourceId) {
        return Optional.ofNullable(resourceMap.get(resourceId));
    }

    public Optional<UserDatabaseMeta> fetchDatabase(ResourceId databaseId) {
        return Optional.ofNullable(databaseMap.get(databaseId));
    }

    @Override
    public Optional<UserDatabaseMeta> getDatabaseMetadata(ResourceId databaseId, int userId) {
        return fetchDatabase(databaseId);
    }

    @Override
    public Optional<UserDatabaseMeta> getDatabaseMetadata(int databaseId, int userId) {
        return getDatabaseMetadata(CuidAdapter.databaseId(databaseId), userId);
    }

    @Override
    public List<UserDatabaseMeta> getVisibleDatabases(int userId) {
        throw new IllegalArgumentException("TODO");
    }

    @Override
    public Optional<UserDatabaseMeta> getDatabaseMetadataByResource(ResourceId resourceId, int userId) {
        return Optional.ofNullable(resourceMap.get(resourceId));
    }

    @Override
    public Map<ResourceId, UserDatabaseMeta> getDatabaseMetadata(Set<ResourceId> databaseIds, int userId) {
        return databaseIds.stream()
                .map(dbId -> getDatabaseMetadata(dbId, userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(
                        UserDatabaseMeta::getDatabaseId,
                        dbMeta -> dbMeta));
    }

}
