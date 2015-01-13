package org.activityinfo.store.mysql.cursor;

import java.sql.ResultSet;

public interface QueryExecutor {

    public ResultSet query(String sql);
}
