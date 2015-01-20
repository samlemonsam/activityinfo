package org.activityinfo.server.database;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.inject.Provider;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides a connection to the test database.
 * <p/>
 * By default, we connect to the local 'activityinfo-test' database, with
 * username 'root' and password 'adminpwd'.
 * <p/>
 * This can be overridden by adding the 'testDatabaseUrl',
 * 'testDatabaseUsername' and 'testDatabasePassword' properties to the
 * activityinfo.properties file, or add them as system variables.
 */
public class TestConnectionProvider implements Provider<Connection> {

    private static final Logger LOGGER = Logger.getLogger(TestConnectionProvider.class.getName());

    private static final String PASSWORD_PROPERTY = "testDatabasePassword";
    private static final String USERNAME_PROPERTY = "testDatabaseUsername";
    private static final String URL_PROPERTY = "testDatabaseUrl";

    private static final String DEFAULT_PASSWORD = "adminpwd";
    private static final String DEFAULT_USERNAME = "root";
//    private static final String DEFAULT_URL = "jdbc:mysql://localhost/activityinfo-test?useUnicode=true&characterEncoding=utf8";

    // Use java style encoding name as stated here:
    //http://dev.mysql.com/doc/connector-j/en/connector-j-reference-charsets.html
    private static final String DEFAULT_URL = "jdbc:mysql://localhost/activityinfo-test?useUnicode=true&characterEncoding=UTF-8";

    public static String URL, USERNAME, PASSWORD;

    static {
        try {
            Properties activityinfoProperties = new Properties();
            File propertiesFile = new File(System.getProperty("user.home"), "activityinfo.properties");
            if (propertiesFile.exists()) {
                activityinfoProperties.load(new FileInputStream(propertiesFile));
            }

            String urlProperty = activityinfoProperties.getProperty(URL_PROPERTY);
            URL = urlProperty != null ? urlProperty : System.getProperty(URL_PROPERTY, DEFAULT_URL);

            String usernameProperty = activityinfoProperties.getProperty(USERNAME_PROPERTY);
            USERNAME = usernameProperty != null ? usernameProperty :
                    System.getProperty(USERNAME_PROPERTY, DEFAULT_USERNAME);

            String passwordProperty = activityinfoProperties.getProperty(PASSWORD_PROPERTY);
            PASSWORD = passwordProperty != null ? passwordProperty :
                    System.getProperty(PASSWORD_PROPERTY, DEFAULT_PASSWORD);

        } catch (Exception e) {
            throw new RuntimeException("Can't initialize TestConnectionProvider, error loading propertyfile", e);
        }
    }

    @Override
    public Connection get() {
        try {
            LOGGER.info("Opening test database at " + URL);
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
