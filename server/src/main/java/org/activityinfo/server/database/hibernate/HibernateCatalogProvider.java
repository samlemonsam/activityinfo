package org.activityinfo.server.database.hibernate;

import com.google.inject.Provider;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.CompositeCatalog;
import org.activityinfo.store.hrd.HrdCatalog;
import org.activityinfo.store.mysql.MySqlCatalogProvider;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class HibernateCatalogProvider implements Provider<CollectionCatalog> {

    private final MySqlCatalogProvider catalogProvider;
    
    private final Provider<EntityManager> entityManager;
    

    @Inject
    public HibernateCatalogProvider(MySqlCatalogProvider catalogProvider, Provider<EntityManager> entityManager) {
        this.catalogProvider = catalogProvider;
        this.entityManager = entityManager;
    }

    @Override
    public CollectionCatalog get() {
        HrdCatalog datastoreCatalog = new HrdCatalog();
        CollectionCatalog mysqlCatalog = catalogProvider.openCatalog(new HibernateQueryExecutor(entityManager));
       
        return new CompositeCatalog(datastoreCatalog, mysqlCatalog);
    }
}
