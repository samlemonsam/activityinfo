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
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserDatabaseProvider {

    public static final String ROOT_ID = "databases";

    private static final Logger LOGGER = Logger.getLogger(UserDatabaseProvider.class.getName());

    private static final Predicate<DatabaseMeta> deleted() {
        return DatabaseMeta::isDeleted;
    }

    private static final Predicate<DatabaseMeta> owned(int userId) {
        return meta -> meta.getOwnerId() == userId;
    }

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

    public Optional<UserDatabaseMeta> queryDatabaseMeta(@NotNull ResourceId databaseId, int userId) {
        return Optional.ofNullable(queryDatabaseMeta(Collections.singleton(databaseId), userId).get(databaseId));
    }

    public Map<ResourceId,UserDatabaseMeta> queryDatabaseMeta(@NotNull Set<ResourceId> databaseIds, int userId) {
        Set<DatabaseMeta> databaseMeta = new HashSet<>(metaProvider.getDatabaseMeta(databaseIds).values());
        return findGrantsAndBuild(databaseMeta, userId);
    }

    private Map<ResourceId,UserDatabaseMeta> findGrantsAndBuild(Set<DatabaseMeta> databaseMeta, int userId) {
        if (databaseMeta.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ResourceId,UserDatabaseMeta> userDatabaseMeta = new HashMap<>(databaseMeta.size());

        Set<DatabaseMeta> ownedMeta = databaseMeta.stream().filter(owned(userId)).collect(Collectors.toSet());
        Set<DatabaseMeta> deletedMeta = databaseMeta.stream().filter(deleted()).collect(Collectors.toSet());
        Set<DatabaseMeta> grantMeta = databaseMeta.stream().filter(owned(userId).negate()).filter(deleted().negate()).collect(Collectors.toSet());

        if (!ownedMeta.isEmpty()) {
            userDatabaseMeta.putAll(buildOwnedMeta(ownedMeta));
        }
        if (!deletedMeta.isEmpty()) {
            userDatabaseMeta.putAll(buildDeletedMeta(deletedMeta, userId));
        }
        if (!grantMeta.isEmpty()) {
            userDatabaseMeta.putAll(buildGrantedMeta(grantMeta, userId));
        }

        return userDatabaseMeta;
    }

    public Optional<UserDatabaseMeta> queryUserDatabaseMetaByResource(@NotNull ResourceId resourceId, int userId) {
        Optional<DatabaseMeta> databaseMeta = metaProvider.getDatabaseMetaForResource(resourceId);
        if (!databaseMeta.isPresent()) {
            return Optional.empty();
        }
        return databaseMeta.map(db -> findGrantsAndBuild(Collections.singleton(db), userId).get(db.getDatabaseId()));
    }

    private Map<ResourceId,UserDatabaseMeta> buildOwnedMeta(@NotNull Set<DatabaseMeta> databaseMeta) {
        return databaseMeta.stream()
                .map(UserDatabaseMeta::buildOwnedUserDatabaseMeta)
                .collect(Collectors.toMap(UserDatabaseMeta::getDatabaseId, db -> db));
    }

    private Map<ResourceId,UserDatabaseMeta> buildDeletedMeta(@NotNull Set<DatabaseMeta> databaseMeta, int userId) {
        return databaseMeta.stream()
                .map(dbMeta -> UserDatabaseMeta.buildDeletedUserDatabaseMeta(dbMeta,userId))
                .collect(Collectors.toMap(UserDatabaseMeta::getDatabaseId, db -> db));
    }

    private Map<ResourceId,UserDatabaseMeta> buildGrantedMeta(@NotNull Set<DatabaseMeta> databaseMeta, int userId) {
        Set<ResourceId> dbIds = databaseMeta.stream().map(DatabaseMeta::getDatabaseId).collect(Collectors.toSet());
        Map<ResourceId,DatabaseGrant> grants = grantProvider.getDatabaseGrants(userId, dbIds);
        return databaseMeta.stream()
                .map(dbMeta -> {
                    if (grants.containsKey(dbMeta.getDatabaseId())) {
                        return UserDatabaseMeta.buildUserDatabaseMeta(grants.get(dbMeta.getDatabaseId()), dbMeta);
                    }
                    return UserDatabaseMeta.buildGrantlessUserDatabaseMeta(dbMeta, userId);
                })
                .collect(Collectors.toMap(UserDatabaseMeta::getDatabaseId, db -> db));
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
        return databaseGrants.stream().map(grant -> UserDatabaseMeta.buildUserDatabaseMeta(grant, databaseMeta.get(grant.getDatabaseId())));
    }

    private Stream<UserDatabaseMeta> fetchOwnedUserDatabaseMeta(int userId) {
        Map<ResourceId,DatabaseMeta> ownedDatabaseMeta = metaProvider.getOwnedDatabaseMeta(userId);
        if (ownedDatabaseMeta.isEmpty()) {
            return Stream.empty();
        }
        return ownedDatabaseMeta.values().stream().map(UserDatabaseMeta::buildOwnedUserDatabaseMeta);
    }

}
