package org.activityinfo.server.database.hibernate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.database.DatabaseGrantKey;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseGrantCache;
import org.activityinfo.store.spi.DatabaseGrantProvider;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HibernateDatabaseGrantProvider implements DatabaseGrantProvider {

    private static final Logger LOGGER = Logger.getLogger(HibernateDatabaseGrantProvider.class.getName());

    private static final String CACHE_PREFIX = "dbGrant";
    private static final String CACHE_VERSION = "2";

    private final Provider<EntityManager> entityManager;
    private final DatabaseGrantCache cache;

    @Inject
    public HibernateDatabaseGrantProvider(Provider<EntityManager> entityManager,
                                          DatabaseGrantCache cache) {
        this.entityManager = entityManager;
        this.cache = cache;
    }

    @Override
    public Optional<DatabaseGrant> getDatabaseGrant(int userId, @NotNull ResourceId databaseId) {
        return cache.load(DatabaseGrantKey.of(userId,databaseId));
    }

    @Override
    public List<DatabaseGrant> getAllDatabaseGrantsForUser(int userId) {
        Set<DatabaseGrantKey> grantedDatabases = queryGrantedDatabases(userId).stream()
                .map(dbId -> DatabaseGrantKey.of(userId,dbId))
                .collect(Collectors.toSet());
        if (grantedDatabases.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(cache.loadAll(grantedDatabases).values());
    }

    @Override
    public List<DatabaseGrant> getAllDatabaseGrantsForDatabase(@NotNull ResourceId databaseId) {
        Set<DatabaseGrantKey> grantedUsers = queryGrantedUsers(databaseId).stream()
                .map(userId -> DatabaseGrantKey.of(userId,databaseId))
                .collect(Collectors.toSet());
        if (grantedUsers.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(cache.loadAll(grantedUsers).values());
    }

    private Set<ResourceId> queryGrantedDatabases(int userId) {
        return entityManager.get().createQuery("SELECT up.database.id " +
                "FROM UserPermission up " +
                "WHERE up.user.id=:userId " +
                "AND up.allowView = TRUE", Integer.class)
                .setParameter("userId", userId)
                .getResultList().stream()
                .map(CuidAdapter::databaseId)
                .collect(Collectors.toSet());
    }

    private Set<Integer> queryGrantedUsers(ResourceId databaseId) {
        return new HashSet<>(entityManager.get().createQuery("SELECT up.user.id " +
                "FROM UserPermission up " +
                "WHERE up.database.id=:databaseId " +
                "AND up.allowView = TRUE", Integer.class)
                .setParameter("databaseId", CuidAdapter.getLegacyIdFromCuid(databaseId))
                .getResultList());
    }

}
