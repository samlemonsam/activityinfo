package org.activityinfo.store.mysql;

import org.activityinfo.store.mysql.cursor.QueryExecutor;


public class MySqlCatalogProvider {

    public MySqlCatalogProvider() {
    }

    public MySqlCatalog openCatalog(final QueryExecutor executor) {
        return new MySqlCatalog(executor);
    }

}
