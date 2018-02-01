package org.activityinfo.server.database.hibernate;

import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.AbstractReturningWork;
import org.hibernate.jdbc.ReturningWork;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HibernateQueryExecutor implements QueryExecutor {
    
    private static final Logger LOGGER = Logger.getLogger(HibernateQueryExecutor.class.getName());
    
    private final Provider<EntityManager> entityManager;

    /**
     * Holds the entity manager for this request, if one has been needed.
     */
    private static final ThreadLocal<HibernateEntityManager> REQUEST_ENTITY_MANAGER = new ThreadLocal<>();

    @Inject
    public HibernateQueryExecutor(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    private <T> T doWork(ReturningWork<T> worker) {

        HibernateEntityManager entityManager = REQUEST_ENTITY_MANAGER.get();
        if(entityManager == null) {
            entityManager = (HibernateEntityManager) this.entityManager.get();
            entityManager.getTransaction().begin();
            REQUEST_ENTITY_MANAGER.set(entityManager);
        }

        return entityManager.getSession().doReturningWork(worker);
    }

    public static void commitIfOpen() {
        HibernateEntityManager entityManager = REQUEST_ENTITY_MANAGER.get();
        if(entityManager != null) {
            REQUEST_ENTITY_MANAGER.remove();
            entityManager.getTransaction().commit();
        }
    }

    public static void rollbackIfOpen() {
        HibernateEntityManager entityManager = REQUEST_ENTITY_MANAGER.get();
        if(entityManager != null) {
            REQUEST_ENTITY_MANAGER.remove();
            try {
                entityManager.getTransaction().rollback();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception thrown while rolling back transaction", e);
            }
        }
    }

    @Override
    public ResultSet query(String sql, Object... parameters) {
        return query(sql, Arrays.asList(parameters));
    }

    @Override
    public ResultSet query(final String sql, final List<?> parameters) {
        LOGGER.fine("Executing query: " + sql);

        return doWork(new AbstractReturningWork<ResultSet>() {
            @Override
            public ResultSet execute(Connection connection) throws SQLException {
                try {
                    PreparedStatement statement = prepare(connection, sql, parameters);
                    return statement.executeQuery();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public int update(final String sql, final List<?> parameters) {
        return doWork(new AbstractReturningWork<Integer>() {
            @Override
            public Integer execute(Connection connection) throws SQLException {
                LOGGER.info("Isolation = " + connection.getTransactionIsolation());

                PreparedStatement statement = prepare(connection, sql, parameters);
                return statement.executeUpdate();
            }
        });
    }

    private PreparedStatement prepare(Connection connection, String sql, List<?> parameters) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
        return statement;
    }
}

