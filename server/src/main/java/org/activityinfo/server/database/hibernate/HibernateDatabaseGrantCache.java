package org.activityinfo.server.database.hibernate;

import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.DatabaseGrantCache;
import org.activityinfo.store.spi.DatabaseMetaCache;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class HibernateDatabaseGrantCache implements DatabaseGrantCache {

    private final Map<String,String> memcacheKeyMap = new HashMap<>();
    private final Map<String,DatabaseGrant> databaseGrantMap = new HashMap<>();

    public HibernateDatabaseGrantCache() {
    }

    public static HibernateDatabaseGrantCache newSession() {
        return new HibernateDatabaseGrantCache();
    }

    @Override
    public String key(int userId, ResourceId databaseId) {
        return userId + ":" + databaseId.toString();
    }

    @Override
    public Map<String,DatabaseGrant> loadAll(Map<String,String> toLoad) {
        Set<String> inCache = toLoad.entrySet().stream()
                .filter(this::isCached)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (inCache.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String,DatabaseGrant> loadedFromCache = new HashMap<>(inCache.size());
        loadedFromCache.putAll(inCache.stream()
                .map(databaseGrantMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        grant -> key(grant.getUserId(),grant.getDatabaseId()),
                        grant -> grant)));

        return loadedFromCache;
    }

    /**
     * Checks if the requested grant is stored in cache with the specified cache key
     */
    private boolean isCached(Map.Entry<String,String> grantCacheVersion) {
        return memcacheKeyMap.containsKey(grantCacheVersion.getKey())
                && memcacheKeyMap.get(grantCacheVersion.getKey()).equals(grantCacheVersion.getValue());
    }

    @Override
    public void putAll(Map<String,DatabaseGrant> toStore) {
        toStore.forEach((memcacheKey,grant) -> {
            memcacheKeyMap.put(key(grant.getUserId(),grant.getDatabaseId()), memcacheKey);
            databaseGrantMap.put(key(grant.getUserId(),grant.getDatabaseId()), grant);
        });
    }

    protected void clear() {
        memcacheKeyMap.clear();
        databaseGrantMap.clear();
    }
}
