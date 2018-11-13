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
import org.activityinfo.store.spi.DatabaseProvider;

import java.util.*;

public class TestingDatabaseProvider implements DatabaseProvider {

    private Map<ResourceId, UserDatabaseMeta> databaseMap = new HashMap<>();
    private Map<ResourceId, UserDatabaseMeta> resourceMap = new HashMap<>();

    public TestingDatabaseProvider() {
    }

    public void add(UserDatabaseMeta database) {
        databaseMap.put(database.getDatabaseId(), database);
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
    public UserDatabaseMeta getDatabaseMetadata(ResourceId databaseId, int userId) {
        Optional<UserDatabaseMeta> database = fetchDatabase(databaseId);
        assert database.isPresent() : "Database was not added to test provider...";
        return database.get();
    }

    @Override
    public UserDatabaseMeta getDatabaseMetadata(int databaseId, int userId) {
        return getDatabaseMetadata(CuidAdapter.databaseId(databaseId), userId);
    }
}
