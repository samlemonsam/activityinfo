package org.activityinfo.store.mysql.cursor;

import java.sql.ResultSet;
import java.util.List;

/**
 * Internal interface for the MySqlCatalog that helps delegate
 * query execution to our environment.
 */
public interface QueryExecutor {

    ResultSet query(String sql);

    int update(String sql, List<?> parameters);
}
