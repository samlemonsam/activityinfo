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
import java.util.logging.Logger;


public class HibernateQueryExecutor implements QueryExecutor {
    
    private static final Logger LOGGER = Logger.getLogger(HibernateQueryExecutor.class.getName());
    
    private final Provider<EntityManager> entityManager;

    @Inject
    public HibernateQueryExecutor(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    private <T> T doWork(ReturningWork<T> worker) {
        HibernateEntityManager hibernateEntityManager = (HibernateEntityManager) entityManager.get();
        return hibernateEntityManager.getSession().doReturningWork(worker);
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
                PreparedStatement statement = prepare(connection, sql, parameters);
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public void begin() {
        entityManager.get().getTransaction().begin();
    }

    @Override
    public void commit() {
        entityManager.get().getTransaction().commit();
    }

    @Override
    public void rollback() {
        entityManager.get().getTransaction().rollback();
    }

    private PreparedStatement prepare(Connection connection, String sql, List<?> parameters) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
        return statement;
    }
}

