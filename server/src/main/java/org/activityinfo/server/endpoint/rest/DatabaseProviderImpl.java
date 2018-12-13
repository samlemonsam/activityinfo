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
package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseProvider;

import java.util.List;
import java.util.Optional;

public class DatabaseProviderImpl implements DatabaseProvider {

    private final GeoDatabaseProvider geoDbProvider;
    private final UserDatabaseProvider userDbProvider;

    @Inject
    public DatabaseProviderImpl(GeoDatabaseProvider geoDbProvider,
                                UserDatabaseProvider userDbProvider) {
        this.geoDbProvider = geoDbProvider;
        this.userDbProvider = userDbProvider;
    }

    @Override
    public Optional<UserDatabaseMeta> getDatabaseMetadata(ResourceId databaseId, int userId) {
        if (geoDbProvider.accept(databaseId)) {
            return geoDbProvider.queryGeoDb(userId);
        } else {
            return userDbProvider.queryDatabaseMeta(databaseId, userId);
        }
    }

    @Override
    public Optional<UserDatabaseMeta> getDatabaseMetadata(int databaseId, int userId) {
        return getDatabaseMetadata(CuidAdapter.databaseId(databaseId), userId);
    }

    @Override
    public List<UserDatabaseMeta> getVisibleDatabases(int userId) {
        return userDbProvider.queryVisibleUserDatabaseMeta(userId);
    }

    @Override
    public Optional<UserDatabaseMeta> getDatabaseMetadataByResource(ResourceId resourceId, int userId) {
        if (geoDbProvider.accept(resourceId)) {
            return geoDbProvider.queryGeoDb(userId);
        } else {
            return userDbProvider.queryUserDatabaseMetaByResource(resourceId, userId);
        }
    }

}
