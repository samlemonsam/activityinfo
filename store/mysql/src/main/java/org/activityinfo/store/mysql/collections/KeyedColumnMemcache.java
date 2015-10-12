package org.activityinfo.store.mysql.collections;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ColumnCache;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class KeyedColumnMemcache implements ColumnCache {

    private static final Logger LOGGER = Logger.getLogger(KeyedColumnMemcache.class.getName());
    
    private final MemcacheService memcacheService;
    
    private ResourceId formClassId;
    private long version;

    public KeyedColumnMemcache() {
        memcacheService = MemcacheServiceFactory.getMemcacheService();
    }

    @Override
    public Map<String, ColumnView> getIfPresent(Set<String> columnIds) {
        try {
            Set<String> keys = new HashSet<>();
            for (String columnId : columnIds) {
                keys.add(key(columnId));
            }
            Map<String, Object> result = memcacheService.getAll(keys);
            Map<String, ColumnView> views = new HashMap<>();
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                views.put(entry.getKey(), (ColumnView) entry.getValue());
            }
            return views;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception fetching column for " + formClassId + " from cache", e);
            return Collections.emptyMap();
        }
    }

    private String key(String columnId) {
        return formClassId.asString() + "." + version + "." + columnId;
    }

    @Override
    public void put(Map<String, ColumnView> columnMap) {
        try {
            Map<String, ColumnView> toCache = new HashMap<>();
            for (Map.Entry<String, ColumnView> entry : columnMap.entrySet()) {
                toCache.put(key(entry.getKey()), entry.getValue());
            }

            memcacheService.putAll(toCache, Expiration.byDeltaSeconds((int) TimeUnit.HOURS.toSeconds(8)));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception caching columns from " + formClassId, e);
        }
    }
}
