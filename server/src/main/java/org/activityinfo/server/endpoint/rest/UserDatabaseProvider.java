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
import org.activityinfo.model.database.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseGrantProvider;
import org.activityinfo.store.spi.DatabaseMetaProvider;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserDatabaseProvider {

    public static final String ROOT_ID = "databases";

    private static final Logger LOGGER = Logger.getLogger(UserDatabaseProvider.class.getName());

    private final DatabaseGrantProvider grantProvider;
    private final DatabaseMetaProvider metaProvider;

    @Inject
    public UserDatabaseProvider(DatabaseGrantProvider grantProvider,
                                DatabaseMetaProvider metaProvider) {
        this.grantProvider = grantProvider;
        this.metaProvider = metaProvider;
    }

    public List<UserDatabaseMeta> queryVisibleUserDatabaseMeta(int userId) {
        return Stream
                .concat(fetchAssignedUserDatabaseMeta(userId),
                        fetchOwnedUserDatabaseMeta(userId))
                .collect(Collectors.toList());
    }

    public UserDatabaseMeta queryDatabaseMeta(ResourceId databaseId, int userId) {
        DatabaseMeta databaseMeta = metaProvider.getDatabaseMeta(databaseId);
        if (databaseMeta == null) {
            LOGGER.severe(String.format("Database %s requested by User %d does not exist", databaseId.asString(), userId));
            throw new IllegalArgumentException();
        }
        return findGrantAndBuildMeta(databaseMeta, userId);
    }

    public UserDatabaseMeta queryUserDatabaseMetaByResource(ResourceId resourceId, int userId) {
        DatabaseMeta databaseMeta = metaProvider.getDatabaseMetaForResource(resourceId);
        if (databaseMeta == null) {
            LOGGER.severe(String.format("Database with Resource %s requested by User %d does not exist", resourceId.asString(), userId));
            throw new IllegalArgumentException();
        }
        return findGrantAndBuildMeta(databaseMeta, userId);
    }

    private UserDatabaseMeta findGrantAndBuildMeta(DatabaseMeta databaseMeta, int userId) {
        if (databaseMeta.getOwnerId() == userId) {
            return buildOwnedUserDatabaseMeta(databaseMeta);
        }
        DatabaseGrant databaseGrant = grantProvider.getDatabaseGrant(userId, databaseMeta.getDatabaseId());
        if (databaseGrant == null) {
            return buildGrantlessUserDatabaseMeta(databaseMeta, userId);
        }
        return buildUserDatabaseMeta(databaseGrant, databaseMeta);
    }

    private Stream<UserDatabaseMeta> fetchAssignedUserDatabaseMeta(int userId) {
        List<DatabaseGrant> databaseGrants = grantProvider.getAllDatabaseGrantsForUser(userId);
        Set<ResourceId> assignedDatabaseIds = databaseGrants.stream().map(DatabaseGrant::getDatabaseId).collect(Collectors.toSet());
        Map<ResourceId,DatabaseMeta> databaseMeta = metaProvider.getDatabaseMeta(assignedDatabaseIds);
        return databaseGrants.stream().map(grant -> buildUserDatabaseMeta(grant, databaseMeta.get(grant.getDatabaseId())));
    }

    private Stream <UserDatabaseMeta> fetchOwnedUserDatabaseMeta(int userId) {
        return metaProvider.getOwnedDatabaseMeta(userId).values().stream()
                .map(UserDatabaseProvider::buildOwnedUserDatabaseMeta);
    }

    private static UserDatabaseMeta buildOwnedUserDatabaseMeta(DatabaseMeta databaseMeta) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseMeta.getDatabaseId())
                .setUserId(databaseMeta.getOwnerId())
                .setOwner(true)
                .setLabel(databaseMeta.getLabel())
                .setPublished(databaseMeta.isPublished())
                .setVersion(Long.toString(databaseMeta.getVersion()))
                .setPendingTransfer(databaseMeta.isPendingTransfer())
                .addResources(databaseMeta.getResources().values())
                .addLocks(databaseMeta.getLocks().values())
                .build();
    }

    private static UserDatabaseMeta buildUserDatabaseMeta(DatabaseGrant databaseGrant, DatabaseMeta databaseMeta) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseMeta.getDatabaseId())
                .setUserId(databaseGrant.getUserId())
                .setOwner(databaseGrant.getUserId() == databaseMeta.getOwnerId())
                .setLabel(databaseMeta.getLabel())
                .setPublished(databaseMeta.isPublished())
                .setVersion(version(databaseMeta.getVersion(), databaseGrant.getVersion()))
                .addResources(databaseMeta.getResources().values())
                .addLocks(databaseMeta.getLocks().values())
                .addGrants(databaseGrant.getGrants().values())
                .build();
    }

    private static UserDatabaseMeta buildGrantlessUserDatabaseMeta(DatabaseMeta databaseMeta, int userId) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseMeta.getDatabaseId())
                .setUserId(userId)
                .setLabel(databaseMeta.getLabel())
                .setPublished(databaseMeta.isPublished())
                .setVersion(Long.toString(databaseMeta.getVersion()))
                .addResources(databaseMeta.getResources().values())
                .addLocks(databaseMeta.getLocks().values())
                .build();
    }

    private static String version(long databaseVersion, long grantVersion) {
        return Long.toString(databaseVersion) + UserDatabaseMeta.VERSION_SEP + Long.toString(grantVersion);
    }

}
