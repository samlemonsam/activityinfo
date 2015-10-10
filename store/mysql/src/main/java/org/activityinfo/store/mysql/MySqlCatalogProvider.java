package org.activityinfo.store.mysql;

import com.google.common.collect.Lists;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.store.mysql.collections.*;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.util.List;
import java.util.logging.Logger;


public class MySqlCatalogProvider {

    private static final Logger LOGGER = Logger.getLogger(MySqlCatalogProvider.class.getName());

    private List<CollectionProvider> providers = Lists.newArrayList();

    public MySqlCatalogProvider() {
        providers.add(new SimpleTableCollectionProvider(new DatabaseTable(), CollectionPermissions.readonly()));
        providers.add(new SimpleTableCollectionProvider(new UserTable(), CollectionPermissions.readonly()));
        providers.add(new SimpleTableCollectionProvider(new CountryTable(), CollectionPermissions.readonly()));
        providers.add(new SimpleTableCollectionProvider(new AdminEntityTable(), CollectionPermissions.readonly()));
        providers.add(new SimpleTableCollectionProvider(new PartnerTable(), CollectionPermissions.readonly()));
        providers.add(new SiteCollectionProvider());
        providers.add(new LocationCollectionProvider());
        providers.add(new ReportingPeriodCollectionProvider());
    }

    public CollectionCatalog openCatalog(final QueryExecutor executor) {
        return new MySqlSession(providers, executor);
    }

}
