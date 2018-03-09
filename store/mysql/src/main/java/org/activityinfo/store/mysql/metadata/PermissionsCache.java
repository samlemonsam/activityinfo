/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

        try(ResultSet rs = executor.query("select ownerUserId from userdatabase where databaseId=" + databaseId)) {
            if(rs.next()) {
                int ownerUserId = rs.getInt(1);
                if(ownerUserId == userId) {
                    permission.viewAll = true;
                    permission.view = true;
                    permission.edit = true;
                    permission.editAll = true;
                    permission.design = true;
                    return permission;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String query = format("select partnerId, AllowViewAll, AllowView, AllowEditAll, AllowEdit " +
                        " from userpermission where databaseId=%d and UserId = %d",
                databaseId,
                userId);

        Stopwatch started = Stopwatch.createStarted();
        LOGGER.info("Query: " + query);

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
                if(rs.getBoolean(5)) {
                    permission.design = true;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            LOGGER.info("Completed in " + started.elapsed(TimeUnit.MILLISECONDS) + "ms");
        }

        return permission;
    }
    
}
