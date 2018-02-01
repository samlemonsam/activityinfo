package org.activityinfo.store.mysql;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.LowerCaseDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mysql.MySqlConnection;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class DbUnit {

    private static final Logger LOGGER = Logger.getLogger(DbUnit.class.getName());

    private static final String LIQUIBASE_TABLE_PREFIX = "DATABASECHANGELOG";

    private static final String PASSWORD_PROPERTY = "testDatabasePassword";
    private static final String USERNAME_PROPERTY = "testDatabaseUsername";
    private static final String URL_PROPERTY = "testDatabaseUrl";

    private static Connection connection;

    public void openDatabase() throws ClassNotFoundException, SQLException {
        if(connection == null) {
            String url = Preconditions.checkNotNull(System.getProperty(URL_PROPERTY,
                    "jdbc:mysql://127.0.0.1:3306/aitest?user=root&password=root&zeroDateTimeBehavior=convertToNull&useUnicode=true"));
            String username = Preconditions.checkNotNull(System.getProperty(USERNAME_PROPERTY, "root"), USERNAME_PROPERTY);
            String password = Preconditions.checkNotNull(System.getProperty(PASSWORD_PROPERTY, "root"), PASSWORD_PROPERTY);

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
        }
    }

    public void dropAllRows() throws SQLException {
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        statement.execute("SET foreign_key_checks = 0");

        ResultSet tables = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
        try {
            while (tables.next()) {
                String tableName = tables.getString(3);
                if (!tableName.toLowerCase().startsWith(LIQUIBASE_TABLE_PREFIX)) {
                    statement.execute("DELETE FROM " + tableName);
                    LOGGER.fine("Dropped all from " + tableName);
                }
            }
        } finally {
            tables.close();
        }
        statement.execute("SET foreign_key_checks = 1");
        statement.close();
        connection.commit();

    }

    public QueryExecutor getExecutor() {
        return new QueryExecutor() {
            @Override
            public ResultSet query(String sql, Object... parameters) {
                return query(sql, Arrays.asList(parameters));
            }

            @Override
            public ResultSet query(String sql, List<?> parameters) {
                try {
                    PreparedStatement statement = prepare(sql, parameters);
                    return statement.executeQuery(sql);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public int update(String sql, List<?> parameters) {
                
                System.out.println(sql);
                
                try {
                    PreparedStatement statement = prepare(sql, parameters);
                    int rowsAffected = statement.executeUpdate();
                    
                    return rowsAffected;

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

            private PreparedStatement prepare(String sql, List<?> parameters) throws SQLException {
                System.out.println("SQL: " + sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                for (int i = 0; i < parameters.size(); ++i) {
                    statement.setObject(i + 1, parameters.get(i));
                }
                return statement;
            }
        };
    }

    public void loadDatset(URL resource) throws Throwable {
        InputStream in = Resources.asByteSource(resource).openStream();
        Preconditions.checkNotNull("Cannot find xml file " + resource);

        LowerCaseDataSet dataSet = new LowerCaseDataSet(new FlatXmlDataSetBuilder()
                .setDtdMetadata(true)
                .setColumnSensing(true)
                .build(new InputStreamReader(in)));

        IDatabaseConnection dbUnitConnection = new MySqlConnection(connection, null);

        InsertIdentityOperation.INSERT.execute(dbUnitConnection, dataSet);
    }

    public Connection getConnection() {
        return connection;
    }


}
