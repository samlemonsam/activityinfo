package org.activityinfo.store.mysql.cursor;

import java.sql.ResultSet;
import java.util.List;

public interface QueryExecutor {

    ResultSet query(String sql);

    int update(String sql, List<?> parameters);
}
