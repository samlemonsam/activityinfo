package org.activityinfo.store.mysql;

import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.mysql.cursor.QueryExecutor;


public class MySqlCatalogProvider {

    public MySqlCatalogProvider() {
    }

    public CollectionCatalog openCatalog(final QueryExecutor executor) {
        return new MySqlSession(executor);
    }

}
