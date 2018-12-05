package org.activityinfo.server.database.hibernate;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.json.Json;
import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.database.RecordLock;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.Activity;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Folder;
import org.activityinfo.server.database.hibernate.entity.LockedPeriod;
import org.activityinfo.store.spi.DatabaseMetaProvider;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HibernateDatabaseMetaProvider implements DatabaseMetaProvider {

    private final Logger LOGGER = Logger.getLogger(HibernateDatabaseGrantProvider.class.getName());

    private final Provider<EntityManager> entityManager;
    private final MemcacheService memcacheService;

    @Inject
    public HibernateDatabaseMetaProvider(Provider<EntityManager> entityManager,
                                         MemcacheService memcacheService) {
        this.entityManager = entityManager;
        this.memcacheService = memcacheService;
    }

    @Override
    public Optional<DatabaseMeta> getDatabaseMeta(@NotNull ResourceId databaseId) {
        Long databaseVersion = queryDatabaseVersion(databaseId);
        if (databaseVersion == null) {
            return Optional.empty();
        }
        Map<ResourceId,DatabaseMeta> loaded = loadFromMemcache(Collections.singletonMap(databaseId,databaseVersion));
        if (!loaded.isEmpty()) {
            return Optional.of(loaded.get(databaseId));
        }
        Map<ResourceId,DatabaseMeta> loadedFromDb = loadFromDb(Collections.singleton(databaseId));
        cacheToMemcache(loadedFromDb.values());
        return Optional.of(loadedFromDb.get(databaseId));
    }

    @Override
    public Map<ResourceId,DatabaseMeta> getDatabaseMeta(@NotNull Set<ResourceId> databases) {
        if (databases.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ResourceId,Long> toFetch = queryDatabaseVersions(databases);
        if (toFetch.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ResourceId,DatabaseMeta> loaded = new HashMap<>(databases.size());
        loaded.putAll(loadFromMemcache(toFetch));
        if (loaded.size() == toFetch.size()) {
            return loaded;
        }
        loaded.forEach((dbId,cachedDbMeta) -> toFetch.remove(dbId));
        Map<ResourceId,DatabaseMeta> loadedFromDb = loadFromDb(toFetch.keySet());
        cacheToMemcache(loadedFromDb.values());
        loaded.putAll(loadedFromDb);
        return loaded;
    }

    @Override
    public Map<ResourceId, DatabaseMeta> getOwnedDatabaseMeta(int ownerId) {
        Set<ResourceId> ownedDatabases = queryOwnedDatabaseIds(ownerId);
        if (ownedDatabases.isEmpty()) {
            return Collections.emptyMap();
        }
        return getDatabaseMeta(ownedDatabases);
    }

    @Override
    public Optional<DatabaseMeta> getDatabaseMetaForResource(@NotNull ResourceId resourceId) {
        switch(resourceId.getDomain()) {
            case CuidAdapter.DATABASE_DOMAIN:
                return getDatabaseMeta(resourceId);
            case CuidAdapter.ACTIVITY_DOMAIN:
                ResourceId activityDatabaseId = queryDatabaseIdForForm(resourceId);
                return getDatabaseMeta(activityDatabaseId);
            case CuidAdapter.FOLDER_DOMAIN:
                ResourceId folderDatabaseId = queryDatabaseIdForFolder(resourceId);
                return getDatabaseMeta(folderDatabaseId);
            default:
                throw new IllegalArgumentException("Cannot fetch UserDatabaseMeta for Resource: " + resourceId.toString());
        }
    }

    private Map<ResourceId,Long> queryDatabaseVersions(@NotNull Set<ResourceId> databaseIds) {
        Map<ResourceId,Long> versions = databaseIds.stream()
                .collect(Collectors.toMap(
                        dbId -> dbId,
                        this::queryDatabaseVersion));
        return versions.entrySet().stream()
                .filter(dbVersion -> dbVersion.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    private Set<ResourceId> queryOwnedDatabaseIds(int ownerId) {
        return entityManager.get().createQuery("SELECT db.id " +
                "FROM Database db " +
                "WHERE db.owner.id=:ownerId", Integer.class)
                .setParameter("ownerId", ownerId)
                .getResultList().stream()
                .map(CuidAdapter::databaseId)
                .collect(Collectors.toSet());
    }

    private Map<ResourceId,DatabaseMeta> loadFromMemcache(Map<ResourceId,Long> toFetch) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseMeta from Memcache", toFetch.size()));
        Map<ResourceId,DatabaseMeta> loaded = new HashMap<>(toFetch.size());
        Map<String,ResourceId> fetchKeys = toFetch.entrySet().stream()
                .collect(Collectors.toMap(
                        db -> memcacheKey(db.getKey(), db.getValue()),
                        Map.Entry::getKey));
        try {
            Map<String,Object> cached = memcacheService.getAll(fetchKeys.keySet());
            loaded.putAll(cached.entrySet().stream()
                    .collect(Collectors.toMap(
                            cachedDb -> fetchKeys.get(cachedDb.getKey()),
                            cachedDb -> deserialize((String) cachedDb.getValue()))));
        } catch (Exception ignorable) {
            // Memcache load failed, but we can still retrieve from database
            LOGGER.severe(String.format("Fetching failed for %d DatabaseMeta from Memcache", toFetch.size()));
        }
        LOGGER.info(() -> String.format("Fetched %d/%d DatabaseMeta from Memcache", loaded.size(), toFetch.size()));
        return loaded;
    }

    private DatabaseMeta deserialize(String serializedDatabaseMeta) {
        return DatabaseMeta.fromJson(Json.parse(serializedDatabaseMeta));
    }

    private Map<ResourceId,DatabaseMeta> loadFromDb(Set<ResourceId> toFetch) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseMeta from MySqlDatabase", toFetch.size()));
        if (toFetch.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Integer> legacysIds = toFetch.stream()
                .map(CuidAdapter::getLegacyIdFromCuid)
                .collect(Collectors.toList());
        return entityManager.get().createQuery("SELECT db " +
                "FROM Database db " +
                "WHERE db.id IN :databaseIds", Database.class)
                .setParameter("databaseIds", legacysIds)
                .getResultList().stream()
                .map(this::buildMeta)
                .collect(Collectors.toMap(
                        DatabaseMeta::getDatabaseId,
                        dbMeta -> dbMeta));
    }

    private void cacheToMemcache(Collection<DatabaseMeta> databases) {
        LOGGER.info(() -> String.format("Caching %d DatabaseMeta to Memcache", databases.size()));
        Map<String,String> toCache = databases.stream()
                .collect(Collectors.toMap(
                        db -> memcacheKey(db.getDatabaseId(),db.getVersion()),
                        db -> db.toJson().toJson()));
        try {
            memcacheService.putAll(toCache);
        } catch (Exception ignorable) {
            // Caching failed, but is not terminal
            LOGGER.severe(String.format("Caching failed for %d DatabaseMeta to Memcache", databases.size()));
        }
    }

    private static String memcacheKey(ResourceId databaseId, long databaseVersion) {
        return String.format("%s:%d", databaseId.asString(), databaseVersion);
    }

    private @Nullable Long queryDatabaseVersion(ResourceId databaseId) {
        try {
            return entityManager.get().createQuery("SELECT db.version " +
                    "FROM Database db " +
                    "WHERE db.id=:dbId", Long.class)
                    .setParameter("dbId", CuidAdapter.getLegacyIdFromCuid(databaseId))
                    .getSingleResult();
        } catch (NoResultException noDatabase) {
            return null;
        }
    }

    private @Nullable ResourceId queryDatabaseIdForForm(@NotNull ResourceId formId) {
        try {
            int dbId = entityManager.get().createQuery("select form.database.id " +
                    "from Activity form " +
                    "where form.id = :formId", Integer.class)
                    .setParameter("formId", CuidAdapter.getLegacyIdFromCuid(formId))
                    .getSingleResult();
            return CuidAdapter.databaseId(dbId);
        } catch (NoResultException noResult) {
            return null;
        }
    }

    private @Nullable ResourceId queryDatabaseIdForFolder(@NotNull ResourceId folderId) {
        try {
            int dbId = entityManager.get().createQuery("select folder.database.id " +
                    "from Folder folder " +
                    "where folder.id = :folderId", Integer.class)
                    .setParameter("folderId", CuidAdapter.getLegacyIdFromCuid(folderId))
                    .getSingleResult();
            return CuidAdapter.databaseId(dbId);
        } catch (NoResultException noResult) {
            return null;
        }
    }

    private @Nullable DatabaseMeta buildMeta(@Nullable Database database) {
        if (database == null) {
            return null;
        }
        return new DatabaseMeta.Builder()
                .setDatabaseId(CuidAdapter.databaseId(database.getId()))
                .setOwnerId(database.getOwner().getId())
                .setLabel(database.getName())
                .setDescription(database.getFullName())
                .setPublished(false)
                .setPendingTransfer(database.hasPendingTransfer())
                .addResources(fetchResources(database))
                .addLocks(fetchLocks(database))
                .build();
    }

    private List<Resource> fetchResources(@NotNull Database database) {
        Stream<Resource> formResources = fetchForms(database);
        Stream<Resource> folderResources = fetchFolders(database);
        return Stream.concat(formResources, folderResources).collect(Collectors.toList());
    }

    private Stream<Resource> fetchForms(@NotNull Database database) {
        return database.getActivities().stream()
                .filter(a -> !a.isDeleted())
                .map(Activity::asResource);
    }

    private Stream<Resource> fetchFolders(@NotNull Database database) {
        return entityManager.get().createQuery("SELECT f " +
                "FROM Folder f " +
                "WHERE f.database=:database", Folder.class)
                .setParameter("database", database)
                .getResultList().stream()
                .map(Folder::asResource);
    }

    private List<RecordLock> fetchLocks(@NotNull Database database) {
        return database.getLockedPeriods().stream()
                .filter(LockedPeriod::isEnabled)
                .map(LockedPeriod::asDatabaseLock)
                .collect(Collectors.toList());
    }

}
