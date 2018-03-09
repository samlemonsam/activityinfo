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

import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of the current version of user database within a given session
 *
 */
public class DatabaseCacheImpl implements DatabaseCache {
    
    private Map<Integer, Long> map = new HashMap<>();
    private final QueryExecutor executor;


    public DatabaseCacheImpl(QueryExecutor executor) {
        this.executor = executor;
    }
    
    @Override
    public long getSchemaVersion(int databaseId) throws SQLException {
        if(map.containsKey(databaseId)) {
            return map.get(databaseId);
        }
        try(ResultSet rs = executor.query("select version from userdatabase where databaseid=" + databaseId)) {
            if(rs.next()) {
                long version = rs.getLong(1);
                map.put(databaseId, version);
                return version;
            } else {
                return 0;
            }
        }
    }
}
