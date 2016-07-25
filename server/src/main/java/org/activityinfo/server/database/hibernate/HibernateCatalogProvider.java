package org.activityinfo.server.database.hibernate;

import com.google.inject.Provider;
import org.activityinfo.store.mysql.MySqlCatalog;
import org.activityinfo.store.mysql.MySqlCatalogProvider;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Expose the {@link MySqlCatalog} implementation specifically, as there are some remaining dependencies between
 * the pivot table generator and the MySQL implementation. 
 */
public class HibernateCatalogProvider implements Provider<MySqlCatalog> {

    private final MySqlCatalogProvider catalogProvider;
    
    private final Provider<EntityManager> entityManager;
    

    @Inject
    public HibernateCatalogProvider(MySqlCatalogProvider catalogProvider, Provider<EntityManager> entityManager) {
        this.catalogProvider = catalogProvider;
        this.entityManager = entityManager;
    }

    @Override
    public MySqlCatalog get() {
        return catalogProvider.openCatalog(new HibernateQueryExecutor(entityManager));
    }
}
