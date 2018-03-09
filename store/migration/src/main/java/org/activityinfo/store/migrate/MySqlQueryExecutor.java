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
package org.activityinfo.store.migrate;

import com.google.appengine.api.utils.SystemProperty;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySqlQueryExecutor implements QueryExecutor, AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(MySqlQueryExecutor.class.getName());


    private static final String PRODUCTION_DRIVER_CLASS = "com.mysql.jdbc.GoogleDriver";

    private static final String DEVELOPMENT_DRIVER = "com.mysql.jdbc.Driver";

    private transient Driver driver;
    private final Connection connection;

    public MySqlQueryExecutor() throws SQLException {


        Properties properties = new Properties();
        properties.setProperty("useUnicode", "true");
        properties.setProperty("characterEncoding", "utf8");
        properties.setProperty("zeroDateTimeBehavior", "convertToNull");

        String connectionUrl;

        if(SystemProperty.Environment.environment.value() == SystemProperty.Environment.Value.Production) {
            String appName = SystemProperty.applicationId.get();
            if(appName.equals("activityinfoeu")) {
                connectionUrl = "jdbc:google:mysql://bedatadriven.com:activityinfo:production2/activityinfo";
            } else {
                connectionUrl = "jdbc:google:mysql://" + appName + ":activityinfo/activityinfo";
            }
            properties.setProperty("user", "root");

        } else {
            connectionUrl = "jdbc:mysql://localhost/activityinfo";
            properties.setProperty("user", "root");
            properties.setProperty("password", "root");
        }
        Driver driver = getDriver();
        connection = driver.connect(connectionUrl, properties);
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to close connection", e);
        }
    }

    private Driver getDriver() {
        if(driver == null) {
            if (SystemProperty.Environment.environment.value() == SystemProperty.Environment.Value.Production) {
                initProductionDriver();
            } else {
                initDevelopmentDriver();
            }
        }
        return driver;
    }

    private void initProductionDriver() {

        LOGGER.info("Using driver " + PRODUCTION_DRIVER_CLASS);

        try {
            driver = (Driver)Class.forName(PRODUCTION_DRIVER_CLASS).newInstance();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to instantiate " + PRODUCTION_DRIVER_CLASS + ", ensure that " +
                    "<use-google-connector-j>true</use-google-connector-j> is declared in appengine-web.xml", e);
        }
    }


    private void initDevelopmentDriver() {

        LOGGER.info("Using driver " + DEVELOPMENT_DRIVER);

        try {
            driver = (Driver)Class.forName(DEVELOPMENT_DRIVER).newInstance();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to instantiate " + DEVELOPMENT_DRIVER, e);
        }
    }


    @Override
    public ResultSet query(String sql, Object... parameters) {
        return query(sql, Arrays.asList(parameters));
    }

    @Override
    public ResultSet query(String sql, List<?> parameters) {
        try {
            CallableStatement statement = prepareStatement(sql, parameters);
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private CallableStatement prepareStatement(String sql, List<?> parameters) throws SQLException {
        CallableStatement statement = connection.prepareCall(sql);
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
        return statement;
    }

    @Override
    public int update(String sql, List<?> parameters) {
        try {
            CallableStatement statement = prepareStatement(sql, parameters);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
