package org.activityinfo.server.database.hibernate;

import com.google.inject.Inject;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.CompositeCatalog;
import org.activityinfo.store.hrd.HrdCatalog;
import org.activityinfo.store.mysql.MySqlSession;

import javax.inject.Provider;


public class CompositeCatalogProvider implements Provider<CollectionCatalog> {
    
    private Provider<MySqlSession> mySqlCatalogProvider;

    @Inject
    public CompositeCatalogProvider(Provider<MySqlSession> mySqlCatalogProvider) {
        this.mySqlCatalogProvider = mySqlCatalogProvider;
    }

    @Override
    public CollectionCatalog get() {
        return new CompositeCatalog(new HrdCatalog(), mySqlCatalogProvider.get());
    }
}
