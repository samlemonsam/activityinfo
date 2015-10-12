package org.activityinfo.store.mysql;

import com.google.common.collect.Lists;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.mysql.collections.CollectionProvider;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.util.List;
import java.util.logging.Logger;


public class MySqlCatalogProvider {

    private static final Logger LOGGER = Logger.getLogger(MySqlCatalogProvider.class.getName());

    private List<CollectionProvider> providers = Lists.newArrayList();

    public MySqlCatalogProvider() {
    }

    public CollectionCatalog openCatalog(final QueryExecutor executor) {
        return new MySqlSession(executor);
    }

}
