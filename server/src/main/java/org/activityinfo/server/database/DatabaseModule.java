package org.activityinfo.server.database;

import org.activityinfo.server.endpoint.rest.DatabaseCatalogProvider;
import org.activityinfo.server.endpoint.rest.UserDatabaseProviderImpl;
import org.activityinfo.server.util.jaxrs.AbstractRestModule;
import org.activityinfo.store.spi.UserDatabaseProvider;
import org.activityinfo.store.spi.FormCatalog;

public class DatabaseModule extends AbstractRestModule {

    @Override
    protected void configureResources() {
        bind(UserDatabaseProvider.class).to(UserDatabaseProviderImpl.class);
        bind(FormCatalog.class).to(DatabaseCatalogProvider.class);
    }

}
