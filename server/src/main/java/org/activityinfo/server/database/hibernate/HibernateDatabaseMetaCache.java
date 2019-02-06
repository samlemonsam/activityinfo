package org.activityinfo.server.database.hibernate;

import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseMetaCache;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class HibernateDatabaseMetaCache implements DatabaseMetaCache {

    private final Map<ResourceId,String> memcacheKeyMap = new HashMap<>();
    private final Map<ResourceId,DatabaseMeta> databaseMetaMap = new HashMap<>();

    public HibernateDatabaseMetaCache() {
    }

    public static HibernateDatabaseMetaCache newSession() {
        return new HibernateDatabaseMetaCache();
    }

    @Override
    public Map<ResourceId,DatabaseMeta> loadAll(@NotNull Map<ResourceId,String> toLoad) {
        Set<ResourceId> inCache = toLoad.entrySet().stream()
                .filter(this::isCached)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (inCache.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<ResourceId,DatabaseMeta> loadedFromCache = new HashMap<>(inCache.size());
        loadedFromCache.putAll(inCache.stream()
                .map(databaseMetaMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DatabaseMeta::getDatabaseId, db -> db)));

        return loadedFromCache;
    }

    /**
     * Checks if the requested database is stored in cache with the specified cache key
     */
    private boolean isCached(Map.Entry<ResourceId,String> dbCacheVersion) {
        return memcacheKeyMap.containsKey(dbCacheVersion.getKey())
                && memcacheKeyMap.get(dbCacheVersion.getKey()).equals(dbCacheVersion.getValue());
    }

    @Override
    public void putAll(Map<String,DatabaseMeta> toStore) {
        toStore.forEach((memcacheKey,db) -> {
            memcacheKeyMap.put(db.getDatabaseId(), memcacheKey);
            databaseMetaMap.put(db.getDatabaseId(), db);
        });
    }

    protected void clear() {
        memcacheKeyMap.clear();
        databaseMetaMap.clear();
    }
}
