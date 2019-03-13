package org.activityinfo.server.database.hibernate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.database.DatabaseGrantKey;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseGrantLoader;
import org.activityinfo.store.spi.DatabaseGrantProvider;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DatabaseGrantProvider for DatabaseGrants stored in MySQL.
 */
public class HibernateDatabaseGrantProvider implements DatabaseGrantProvider {

    private final Provider<EntityManager> entityManager;
    private final DatabaseGrantLoader loader;

    @Inject
    public HibernateDatabaseGrantProvider(Provider<EntityManager> entityManager,
                                          DatabaseGrantLoader loader) {
        this.entityManager = entityManager;
        this.loader = loader;
    }

    @Override
    public Optional<DatabaseGrant> getDatabaseGrant(int userId, @NotNull ResourceId databaseId) {
        return loader.load(DatabaseGrantKey.of(userId,databaseId));
    }

    @Override
    public Map<ResourceId, DatabaseGrant> getDatabaseGrants(int userId, Set<ResourceId> databaseIds) {
        Set<DatabaseGrantKey> keys = databaseIds.stream()
                .map(dbId -> DatabaseGrantKey.of(userId, dbId))
                .collect(Collectors.toSet());
        return loader.loadAll(keys).entrySet().stream()
                .collect(Collectors.toMap(
                        grant -> grant.getKey().getDatabaseId(),
                        Map.Entry::getValue));
    }

    @Override
    public List<DatabaseGrant> getAllDatabaseGrantsForUser(int userId) {
        Set<DatabaseGrantKey> grantedDatabases = queryGrantedDatabases(userId).stream()
                .map(dbId -> DatabaseGrantKey.of(userId,dbId))
                .collect(Collectors.toSet());
        if (grantedDatabases.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(loader.loadAll(grantedDatabases).values());
    }

    @Override
    public List<DatabaseGrant> getAllDatabaseGrantsForDatabase(@NotNull ResourceId databaseId) {
        Set<DatabaseGrantKey> grantedUsers = queryGrantedUsers(databaseId).stream()
                .map(userId -> DatabaseGrantKey.of(userId,databaseId))
                .collect(Collectors.toSet());
        if (grantedUsers.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(loader.loadAll(grantedUsers).values());
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
