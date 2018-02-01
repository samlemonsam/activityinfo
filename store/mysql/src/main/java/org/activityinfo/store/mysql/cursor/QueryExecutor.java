package org.activityinfo.store.mysql.cursor;

import java.sql.ResultSet;
import java.util.List;

/**
 * Internal interface for the MySqlCatalog that helps delegate
 * query execution to our environment.
 */
public interface QueryExecutor {

    ResultSet query(String sql, Object... parameters);
    
    ResultSet query(String sql, List<?> parameters);

    int update(String sql, List<?> parameters);

    void begin();

    void commit();

    void rollback();

}
