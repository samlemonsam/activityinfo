package org.activityinfo.server.database;

import com.google.inject.Provider;
import com.google.inject.Provides;
import org.activityinfo.server.endpoint.rest.DatabaseCatalogProvider;
import org.activityinfo.server.endpoint.rest.DatabaseProviderImpl;
import org.activityinfo.server.endpoint.rest.GeoDatabaseProvider;
import org.activityinfo.server.endpoint.rest.UserDatabaseProvider;
import org.activityinfo.server.util.jaxrs.AbstractRestModule;
import org.activityinfo.store.spi.DatabaseProvider;
import org.activityinfo.store.spi.FormCatalog;

import javax.persistence.EntityManager;

public class DatabaseModule extends AbstractRestModule {

    @Override
    protected void configureResources() {
        bind(DatabaseProvider.class).to(DatabaseProviderImpl.class);
        bind(FormCatalog.class).to(DatabaseCatalogProvider.class);
    }

    @Provides
    protected GeoDatabaseProvider provideGeoDatabaseProvider(Provider<EntityManager> em) {
        return new GeoDatabaseProvider(em);
    }

    @Provides
    protected UserDatabaseProvider provideUserDatabaseProvider(Provider<EntityManager> em) {
        return new UserDatabaseProvider(em);
    }

}
