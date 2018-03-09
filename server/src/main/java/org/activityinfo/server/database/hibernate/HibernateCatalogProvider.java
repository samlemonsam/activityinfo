/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.database.hibernate;

import com.google.inject.Provider;
import org.activityinfo.store.mysql.MySqlCatalogProvider;
import org.activityinfo.store.mysql.MySqlStorageProvider;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Expose the {@link MySqlStorageProvider} implementation specifically, as there are some remaining dependencies between
 * the pivot table generator and the MySQL implementation. 
 */
public class HibernateCatalogProvider implements Provider<MySqlStorageProvider> {

    private final MySqlCatalogProvider catalogProvider;
    
    private final Provider<EntityManager> entityManager;
    

    @Inject
    public HibernateCatalogProvider(MySqlCatalogProvider catalogProvider, Provider<EntityManager> entityManager) {
        this.catalogProvider = catalogProvider;
        this.entityManager = entityManager;
    }

    @Override
    public MySqlStorageProvider get() {
        return catalogProvider.openCatalog(new HibernateQueryExecutor(entityManager));
    }
}
