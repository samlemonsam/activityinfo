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

import org.activityinfo.model.database.RecordLock;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DatabaseProviderImpl implements DatabaseProvider {

    public static final ResourceId GEODB_ID = ResourceId.valueOf("geodb");
    private Provider<EntityManager> entityManager;
    private final BillingAccountOracle billingOracle;

    @Inject
    public DatabaseProviderImpl(Provider<EntityManager> entityManager, BillingAccountOracle billingOracle) {
        this.entityManager = entityManager;
        this.billingOracle = billingOracle;
    }


    @Override
    public UserDatabaseMeta getDatabaseMetadata(ResourceId databaseId, int userId) {
        if(databaseId.equals(GEODB_ID)) {
            return queryGeoDb(userId);
        } else {
            return queryMySQLDatabase(databaseId, userId);
        }
    }

    private UserDatabaseMeta queryGeoDb(int userId) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(GEODB_ID)
                .setUserId(userId)
                .setLabel("Geographic Database")
                .setOwner(false)
                .setVersion("1")
                .build();
    }

    private UserDatabaseMeta queryMySQLDatabase(ResourceId databaseResourceId, int userId) {
        UserDatabaseMeta.Builder meta = new UserDatabaseMeta.Builder()
                .setDatabaseId(databaseResourceId)
                .setUserId(userId);


        int databaseId = CuidAdapter.getLegacyIdFromCuid(databaseResourceId);
        Database database = entityManager.get().find(Database.class, databaseId);
        if(database != null) {

            if (database.getOwner().getId() == userId) {
                meta.setOwner(true);
                meta.setVersion(Long.toString(database.getVersion()));
            } else {
                Optional<UserPermission> userPermission = getUserPermission(entityManager.get(), database, userId);
                if(userPermission.isPresent()) {
                    meta.addGrants(userPermission.get().getGrants());
                    meta.setVersion(database.getVersion() + "#" + userPermission.get().getVersion());
                }
            }

            if (meta.isVisible()) {
                meta.setLabel(database.getName());
                meta.addLocks(queryLocks(database));
                meta.addResources(queryFolders(database));
                meta.addResources(queryForms(database));
                meta.setSuspended(billingOracle.isDatabaseSuspended(databaseId));
            }
        }
        return meta.build();
    }


    public static Optional<UserPermission> getUserPermission(EntityManager entityManager, Database database, int userId) {
        List<UserPermission> permissions = entityManager
                .createQuery(
                        "select u from UserPermission u where u.user.id = :userId and u" +
                                ".database = :db",
                        UserPermission.class)
                .setParameter("userId", userId)
                .setParameter("db", database)
                .getResultList();


        if (permissions.isEmpty()) {
            // return a permission with nothing enabled
            return Optional.empty();

        } else {
            return Optional.of(permissions.get(0));
        }
    }

    public List<RecordLock> queryLocks(Database database) {
        return entityManager.get()
                .createQuery("select k from LockedPeriod k where k.database = :database", LockedPeriod.class)
                .setParameter("database", database)
                .getResultList()
                .stream()
                .filter(LockedPeriod::isEnabled)
                .map(LockedPeriod::asDatabaseLock)
                .collect(Collectors.toList());
    }

    public List<Resource> queryFolders(Database database) {
        return entityManager.get()
                .createQuery("select f from Folder f where f.database = :database", Folder.class)
                .setParameter("database", database)
                .getResultList()
                .stream()
                .map(Folder::asResource)
                .collect(Collectors.toList());
    }


    private List<Resource> queryForms(Database database) {
        return database.getActivities()
                .stream()
                .filter(a -> !a.isDeleted())
                .map(Activity::asResource)
                .collect(Collectors.toList());
    }

}
