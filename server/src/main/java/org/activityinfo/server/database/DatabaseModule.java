package org.activityinfo.server.database;

import org.activityinfo.server.endpoint.rest.DatabaseProviderImpl;
import org.activityinfo.server.util.jaxrs.AbstractRestModule;
import org.activityinfo.store.spi.DatabaseProvider;

public class DatabaseModule extends AbstractRestModule {

    @Override
    protected void configureResources() {
        bind(DatabaseProvider.class).to(DatabaseProviderImpl.class);
    }
}
