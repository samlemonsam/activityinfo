package org.activityinfo.server.database.hibernate;

import com.google.common.collect.Lists;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.mysql.MySqlCatalogProvider;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


public class HibernateQueryExecutor  {
    private final Provider<EntityManager> entityManager;
    private final MySqlCatalogProvider catalogProvider;

    public interface StoreSession<T> {
        T execute(CollectionCatalog catalog);
    }

    @Inject
    public HibernateQueryExecutor(Provider<EntityManager> entityManager, MySqlCatalogProvider catalogProvider) {
        this.catalogProvider = catalogProvider;
        this.entityManager = entityManager;
    }

    public <T> T doWork(final StoreSession<T> session) {
        final List<T> collector = Lists.newArrayList();
        HibernateEntityManager hem = (HibernateEntityManager) entityManager.get();
        hem.getSession().doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                QueryExecutor executor = new QueryExecutorImpl(connection);
                CollectionCatalog catalog = catalogProvider.openCatalog(executor);
                collector.add(session.execute(catalog));
            }
        });
        return collector.get(0);
    }

    private class QueryExecutorImpl implements QueryExecutor {
        private final Connection connection;

        public QueryExecutorImpl(Connection connection) {
            this.connection = connection;
        }

        @Override
        public ResultSet query(String sql) {
            try {
                Statement statement = connection.createStatement();
                return statement.executeQuery(sql);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
