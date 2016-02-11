package org.activityinfo.store.mysql.metadata;

import com.google.common.collect.Maps;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static java.lang.String.format;

/**
 * Caches permissions that a single user has
 */
public class PermissionsCache {
    
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
        
        try(ResultSet rs = executor.query(format("select * from userpermission where databaseId=%d and UserId = %d",
                databaseId,
                userId))) {

            if(rs.next()) {

                if(rs.getBoolean("AllowViewAll")) {
                    permission.viewAll = true;

                } else if(rs.getBoolean("AllowView")) {
                    permission.view = true;
                    permission.partnerId = rs.getInt("partnerId");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return permission;
    }
    
}
