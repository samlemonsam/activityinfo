package org.activityinfo.store.mysql;

import org.activityinfo.store.mysql.cursor.QueryExecutor;


public class MySqlCatalogProvider {

    public MySqlCatalogProvider() {
    }

    public MySqlStorageProvider openCatalog(final QueryExecutor executor) {
        return new MySqlStorageProvider(executor);
    }

}
