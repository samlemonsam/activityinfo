package org.activityinfo.store.mysql.metadata;

import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * Caches immutable parent keys
 * 
 * <p>Parent-child schema relationships, such as indicator -> activity or activity -> database, are immutable and so can 
 * be cached indefinitely at the instance level.</p>
 */
public class ParentKeyCache {

    /**
     * An indicator can never be moved to another activity, the mapping from indicator to 
     * activity is immutable, and we can safely cache the relationship on a per-instance basis.
     */
    private static final Cache<Integer, Integer> INDICATOR_TO_ACTIVITY_CACHE = CacheBuilder.newBuilder().build();


    /**
     * An activity's relationship to it's database is also immutable, so we can cache absolutely on 
     * a per-instance basis.
     */
    private static final Cache<Integer, Integer> ACTIVITY_TO_DATABASE_CACHE = CacheBuilder.newBuilder().build();


    private final QueryExecutor executor;


    public ParentKeyCache(QueryExecutor executor) {
        this.executor = executor;
    }
    
    public Map<Integer, Integer> lookupActivityByIndicator(Set<Integer> indicatorIds) throws SQLException {


        // Fetch as many indicators as we can from the instance-level cache
        ImmutableMap<Integer, Integer> cachedMap = INDICATOR_TO_ACTIVITY_CACHE.getAllPresent(indicatorIds);

        // Setup our result set
        Map<Integer, Integer> resultMap = Maps.newHashMap();
        resultMap.putAll(cachedMap);
        
        // Query the database for any remaining indicators
        Set<Integer> toFetch = Sets.difference(indicatorIds, cachedMap.keySet());
        if(!toFetch.isEmpty()) {

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT indicatorId, activityId FROM indicator  " +
                    "WHERE indicatorId IN (");
            Joiner.on(',').appendTo(sql, toFetch);
            sql.append(")");

            try (ResultSet rs = executor.query(sql.toString())) {
                while (rs.next()) {
                    int indicatorId = rs.getInt(1);
                    int activityId = rs.getInt(2);
                    resultMap.put(indicatorId, activityId);
                    INDICATOR_TO_ACTIVITY_CACHE.put(indicatorId, activityId);
                }
            }
        }
        
        return resultMap;
    }

    /**
     * Finds all the activities that belong to the given databaseids
     * 
     * @param databaseIds
     * @return
     * @throws SQLException
     */
    public Set<Integer> queryActivitiesForDatabase(Set<Integer> databaseIds) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.activityId FROM activity a WHERE databaseId IN (");
        Joiner.on(", ").appendTo(sql, databaseIds);
        sql.append(")");

        Set<Integer> activityIds = Sets.newHashSet();

        try(ResultSet rs = executor.query(sql.toString())) {
            while(rs.next()) {
                activityIds.add(rs.getInt(1));
            }
        }

        return activityIds;
    }
}
