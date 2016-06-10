package org.activityinfo.server.database.hibernate;

import com.google.inject.Provider;
import org.activityinfo.store.mysql.MySqlCatalogProvider;
import org.activityinfo.store.mysql.MySqlSession;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Expose the {@link MySqlSession} implementation specifically, as there are some remaining dependencies between
 * the pivot table generator and the MySQL implementation. 
 */
public class HibernateCatalogProvider implements Provider<MySqlSession> {

    private final MySqlCatalogProvider catalogProvider;
    
    private final Provider<EntityManager> entityManager;
    

    @Inject
    public HibernateCatalogProvider(MySqlCatalogProvider catalogProvider, Provider<EntityManager> entityManager) {
        this.catalogProvider = catalogProvider;
        this.entityManager = entityManager;
    }

    @Override
    public MySqlSession get() {
        return catalogProvider.openCatalog(new HibernateQueryExecutor(entityManager));
    }
}
