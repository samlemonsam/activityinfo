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
package org.activityinfo.fixtures;

import com.bedatadriven.rebar.sql.client.query.MySqlDialect;
import com.bedatadriven.rebar.sql.client.query.SqlDialect;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.activityinfo.server.database.TestConnectionProvider;
import org.activityinfo.server.database.hibernate.*;
import org.activityinfo.server.database.hibernate.dao.HibernateDAOModule;
import org.activityinfo.server.database.hibernate.dao.TransactionModule;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.spi.*;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.validator.HibernateValidator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class TestHibernateModule extends AbstractModule {
    private static EntityManagerFactory emf = null;

    @Override
    protected void configure() {

        bind(EntityManager.class).toProvider(EntityManagerProvider.class).in(TestScoped.class);
        bind(MySqlStorageProvider.class).toProvider(HibernateCatalogProvider.class).in(TestScoped.class);
        bind(FormStorageProvider.class).toProvider(HibernateCatalogProvider.class).in(TestScoped.class);

        bind(DatabaseGrantProvider.class).to(HibernateDatabaseGrantProvider.class);
        bind(DatabaseMetaProvider.class).to(HibernateDatabaseMetaProvider.class);
        bind(DatabaseMetaCache.class).to(HibernateDatabaseMetaCache.class);
        bind(DatabaseGrantCache.class).to(HibernateDatabaseGrantCache.class);

        bind(SqlDialect.class).to(MySqlDialect.class);
        bind(Connection.class).toProvider(TestConnectionProvider.class);

        install(new HibernateDAOModule());
        install(new TransactionModule());
    }

    @Provides
    public Session provideHibernateSession(EntityManager em) {
        HibernateEntityManager hem = (HibernateEntityManager) em;
        return hem.getSession();
    }

    @Provides
    @Singleton
    public Validator provideValidator() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        return validatorFactory.getValidator();
    }

    @Provides
    @Singleton
    public EntityManagerFactory provideEntityManagerFactory() {

        // we are assuming that the tests do not affect the database
        // schema, so there is no
        // need to restart hibernate for each test class, and we
        // save quite a bit of time
        if (emf == null) {
            Map<String, String> configOverrides = new HashMap<>();
            configOverrides.put("hibernate.connection.provider_class",
                    TestConnectionProvider.class.getName());

            emf = Persistence.createEntityManagerFactory("ActivityInfo", configOverrides);

            System.err.println("GUICE: EntityManagerFACTORY created");
        }
        return emf;
    }

    @Provides
    protected HibernateDatabaseMetaProvider provideHibernateDatabaseMetaProvider(Provider<EntityManager> entityManager,
                                                                                 FormStorageProvider formStorageProvider,
                                                                                 HibernateDatabaseMetaCache databaseMetaCache,
                                                                                 BillingAccountOracle billingAccountOracle) {
        return new HibernateDatabaseMetaProvider(entityManager,
                formStorageProvider,
                databaseMetaCache,
                MemcacheServiceFactory.getMemcacheService(),
                billingAccountOracle);
    }

    @Provides
    protected HibernateDatabaseGrantProvider provideHibernateDatabaseGrantProvider(Provider<EntityManager> entityManager,
                                                                                   DatabaseGrantCache databaseGrantCache) {
        return new HibernateDatabaseGrantProvider(entityManager,
                databaseGrantCache,
                MemcacheServiceFactory.getMemcacheService());
    }

    @Provides
    @Singleton
    protected HibernateDatabaseMetaCache provideHibernateDatabaseMetaCache() {
        return HibernateDatabaseMetaCache.newSession();
    }

    @Provides
    @Singleton
    protected HibernateDatabaseGrantCache provideHibernateDatabaseGrantCache() {
        return HibernateDatabaseGrantCache.newSession();
    }
    
}
