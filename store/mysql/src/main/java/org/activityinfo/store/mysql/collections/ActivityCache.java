package org.activityinfo.store.mysql.collections;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.service.store.ResourceNotFound;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides three-level caching of {@code Activity}s:
 * 
 * <ol>
 *     <li>Within session</li>
 *     <li>Instance-level</li>
 *     <li>Memcache</li>
 * </ol>
 */
public class ActivityCache {
    
    private static final Logger LOGGER = Logger.getLogger(ActivityCache.class.getName());
    
    private static final MemcacheService MEMCACHE_SERVICE =
            MemcacheServiceFactory.getMemcacheService();
    
    private static final Cache<String, Activity> INSTANCE_CACHE =
            CacheBuilder.newBuilder()
                    .maximumWeight(20000)
                    .weigher(new ActivityWeigher())
                    .expireAfterAccess(3, TimeUnit.HOURS)
                    .build();

    private final Cache<Integer, Activity> sessionCache =
            CacheBuilder.newBuilder()
                    .build();
    private QueryExecutor executor;

    private static class ActivityWeigher implements Weigher<String, Activity> {

        @Override
        public int weigh(String key, Activity value) {
            return value.getFields().size();
        }
    }
    
    public ActivityCache(QueryExecutor executor) {
        this.executor = executor;
    }
    
    public Activity getActivity(int activityId) throws SQLException {
        // We rely on MySQL to provide at least snapshot-level consistency for this transaction,
        // so we should never have to read the activity more than once from the server
        Activity activity = sessionCache.getIfPresent(activityId);
        if(activity != null) {
            LOGGER.fine("Loaded activity " + activityId + " from session cache.");
            return activity;
        }
        
        // Check the current version of the activity database
        long version = queryCurrentVersion(activityId);
        
        activity = INSTANCE_CACHE.getIfPresent(instanceCacheKey(activityId, version));
        if(activity != null) {
            LOGGER.fine("Loaded activity " + activityId + " from instance-level cache.");
            return activity;
        }
        
        // Still not, fetch from memcache
        activity = fetchFromMemcache(activityId, version);
        if(activity != null) {
            LOGGER.fine("Loaded activity " + activityId + " from memcache cache.");
            return activity;
        }
        
        // Ok, have to query from MySQL
        activity = Activity.query(executor, activityId);
        
        // Put to all three cache-levels
        sessionCache.put(activityId, activity);
        INSTANCE_CACHE.put(instanceCacheKey(activityId, activity.getVersion()), activity);
        memcache(activity);
        
        return activity;
    }

    private void memcache(Activity activity) {

        // Store in memcache for subsequent requests
        try {
            MEMCACHE_SERVICE.put(memcacheKey(activity.getId(), activity.getVersion()), activity,
                    Expiration.byDeltaSeconds((int) TimeUnit.HOURS.toSeconds(36)));
                    
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception caching activity " + activity.getId() + " to memcache.", e);
        }
    }

    public long queryCurrentVersion(int activityId) throws SQLException {
        // First do a small query to get the version number of the activity and then fetch from 
        // Memcache if available
        try(ResultSet rs = executor.query("SELECT version FROM activity WHERE activityId = " + activityId)) {
            if (!rs.next()) {
                throw new ResourceNotFound(CuidAdapter.activityFormClass(activityId));
            }
            return rs.getLong(1);
        }
    }
    
    private String instanceCacheKey(int activityId, long version) {
        return activityId + "@" + version;
    }

    private Activity fetchFromMemcache(int activityId, long version) {
        try {
            Activity cachedActivity = (Activity) MEMCACHE_SERVICE.get(memcacheKey(activityId, version));
            if(cachedActivity != null) {
                return cachedActivity;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception deserializing cached Activity " + activityId + " from memcache", e);
        }
        return null;
    }

    private static String memcacheKey(int activityId, long version) {
        return Activity.class.getName() + "#" + activityId + "@" + version;
    }
}
