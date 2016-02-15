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
                permission.partnerId = rs.getInt("partnerId");

                if(rs.getBoolean("AllowViewAll")) {
                    permission.viewAll = true;
                    permission.view = true;

                } else if(rs.getBoolean("AllowView")) {
                    permission.view = true;
                }
                if(rs.getBoolean("AllowEditAll")) {
                    permission.editAll = true;
                    permission.edit = true;
                } else if(rs.getBoolean("AllowEdit")) {
                    permission.edit = true;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return permission;
    }
    
}
