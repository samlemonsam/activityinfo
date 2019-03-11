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
import org.activityinfo.store.spi.UserDatabaseProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DatabaseProvider implementation.
 *
 * Delegates to the {@link GeoDatabaseProvider} for all GeoDatabase requests.
 * Delegates to the {@link DesignedDatabaseProvider} for all other database requests.
 */
public class UserDatabaseProviderImpl implements UserDatabaseProvider {

    private final GeoDatabaseProvider geoDbProvider;
    private final DesignedDatabaseProvider userDbProvider;

    @Inject
    public UserDatabaseProviderImpl(GeoDatabaseProvider geoDbProvider,
                                    DesignedDatabaseProvider userDbProvider) {
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
    public Map<ResourceId, UserDatabaseMeta> getDatabaseMetadata(Set<ResourceId> databaseIds, int userId) {
        Set<ResourceId> geoDbIds = databaseIds.stream().filter(geoDbProvider::accept).collect(Collectors.toSet());
        Set<ResourceId> userDbIds = databaseIds.stream().filter(id -> !geoDbIds.contains(id)).collect(Collectors.toSet());

        return Stream.concat(fetchGeoDbs(geoDbIds, userId), fetchUserDbs(userDbIds, userId))
                .collect(Collectors.toMap(UserDatabaseMeta::getDatabaseId, db -> db));
    }

    private Stream<UserDatabaseMeta> fetchGeoDbs(Set<ResourceId> geoDbIds, int userId) {
        return geoDbIds.stream()
                .map(geoDbId -> geoDbProvider.queryGeoDb(userId))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Stream<UserDatabaseMeta> fetchUserDbs(Set<ResourceId> userDbIds, int userId) {
        return userDbProvider.queryDatabaseMeta(userDbIds, userId).values().stream();
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
