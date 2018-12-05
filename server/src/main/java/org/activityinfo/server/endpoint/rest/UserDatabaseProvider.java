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

import javax.validation.constraints.NotNull;
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

    public UserDatabaseMeta queryDatabaseMeta(@NotNull ResourceId databaseId, int userId) {
        Optional<DatabaseMeta> databaseMeta = metaProvider.getDatabaseMeta(databaseId);
        if (!databaseMeta.isPresent()) {
            return null;
        }
        return findGrantAndBuildMeta(databaseMeta.get(), userId);
    }

    public UserDatabaseMeta queryUserDatabaseMetaByResource(@NotNull ResourceId resourceId, int userId) {
        Optional<DatabaseMeta> databaseMeta = metaProvider.getDatabaseMetaForResource(resourceId);
        if (!databaseMeta.isPresent()) {
            return null;
        }
        return findGrantAndBuildMeta(databaseMeta.get(), userId);
    }

    private UserDatabaseMeta findGrantAndBuildMeta(@NotNull DatabaseMeta databaseMeta, int userId) {
        if (databaseMeta.getOwnerId() == userId) {
            return buildOwnedUserDatabaseMeta(databaseMeta);
        }
        Optional<DatabaseGrant> databaseGrant = grantProvider.getDatabaseGrant(userId, databaseMeta.getDatabaseId());
        if (!databaseGrant.isPresent()) {
            return buildGrantlessUserDatabaseMeta(databaseMeta, userId);
        }
        return buildUserDatabaseMeta(databaseGrant.get(), databaseMeta);
    }

    private Stream<UserDatabaseMeta> fetchAssignedUserDatabaseMeta(int userId) {
        List<DatabaseGrant> databaseGrants = grantProvider.getAllDatabaseGrantsForUser(userId);
        if (databaseGrants.isEmpty()) {
            return Stream.empty();
        }
        Set<ResourceId> assignedDatabaseIds = databaseGrants.stream().map(DatabaseGrant::getDatabaseId).collect(Collectors.toSet());
        Map<ResourceId,DatabaseMeta> databaseMeta = metaProvider.getDatabaseMeta(assignedDatabaseIds);
        if (databaseMeta.isEmpty()) {
            return Stream.empty();
        }
        return databaseGrants.stream().map(grant -> buildUserDatabaseMeta(grant, databaseMeta.get(grant.getDatabaseId())));
    }

    private Stream<UserDatabaseMeta> fetchOwnedUserDatabaseMeta(int userId) {
        Map<ResourceId,DatabaseMeta> ownedDatabaseMeta = metaProvider.getOwnedDatabaseMeta(userId);
        if (ownedDatabaseMeta.isEmpty()) {
            return Stream.empty();
        }
        return ownedDatabaseMeta.values().stream().map(UserDatabaseProvider::buildOwnedUserDatabaseMeta);
    }

    private static UserDatabaseMeta buildOwnedUserDatabaseMeta(@NotNull DatabaseMeta databaseMeta) {
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

    private static UserDatabaseMeta buildUserDatabaseMeta(@NotNull DatabaseGrant databaseGrant, @NotNull DatabaseMeta databaseMeta) {
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

    private static UserDatabaseMeta buildGrantlessUserDatabaseMeta(@NotNull DatabaseMeta databaseMeta, int userId) {
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
