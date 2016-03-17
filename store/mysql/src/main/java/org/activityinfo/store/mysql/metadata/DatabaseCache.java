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
public class DatabaseCache {
    
    private Map<Integer, Long> map = new HashMap<>();
    private final QueryExecutor executor;


    public DatabaseCache(QueryExecutor executor) {
        this.executor = executor;
    }
    
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
