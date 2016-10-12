package org.activityinfo.store.mysql.metadata;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Caches permissions that a single user has
 */
public class PermissionsCache {

    private static final Logger LOGGER = Logger.getLogger(PermissionsCache.class.getName());
    
    private QueryExecutor executor;
        
    private Map<PermissionKey, UserPermission> map = Maps.newHashMap(); 
    
    public PermissionsCache(QueryExecutor executor) {
        this.executor = executor;
    }
    
    public UserPermission getPermission(int userId, int databaseId) {
        PermissionKey key = new PermissionKey(userId, databaseId);
        UserPermission value = map.get(key);
        
        if(value == null) {
            value = query(userId, databaseId);
            map.put(key, value);
        }
        return value;
    }
    
    private UserPermission query(int userId, int databaseId) {

        UserPermission permission = new UserPermission();

        String query = format("select partnerId, AllowViewAll, AllowView, AllowEditAll, AllowEdit " +
                        " from userpermission where databaseId=%d and UserId = %d",
                databaseId,
                userId);

        Stopwatch started = Stopwatch.createStarted();
        LOGGER.fine("Query: " + query);

        try(ResultSet rs = executor.query(query)) {

            if(rs.next()) {
                permission.partnerId = rs.getInt(1);

                if(rs.getBoolean(2)) {
                    permission.viewAll = true;
                    permission.view = true;

                } else if(rs.getBoolean(3)) {
                    permission.view = true;
                }
                if(rs.getBoolean(4)) {
                    permission.editAll = true;
                    permission.edit = true;
                } else if(rs.getBoolean(5)) {
                    permission.edit = true;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            LOGGER.fine("Completed in " + started.elapsed(TimeUnit.MILLISECONDS) + "ms");
        }

        return permission;
    }
    
}
