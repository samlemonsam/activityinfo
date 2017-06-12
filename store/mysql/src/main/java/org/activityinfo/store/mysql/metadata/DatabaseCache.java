package org.activityinfo.store.mysql.metadata;

import java.sql.SQLException;

public interface DatabaseCache {
    long getSchemaVersion(int databaseId) throws SQLException;
}
