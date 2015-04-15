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
import java.util.logging.Logger;

public class DbUnit {

    private static final Logger LOGGER = Logger.getLogger(DbUnit.class.getName());

    private static final String LIQUIBASE_TABLE_PREFIX = "DATABASECHANGELOG";

    private static final String PASSWORD_PROPERTY = "testDatabasePassword";
    private static final String USERNAME_PROPERTY = "testDatabaseUsername";
    private static final String URL_PROPERTY = "testDatabaseUrl";

    private Connection connection;

    public void openDatabase() throws ClassNotFoundException, SQLException {
        String url = Preconditions.checkNotNull(System.getProperty(URL_PROPERTY,
                "jdbc:mysql://127.0.0.1:3306/aitest?user=root&password=root&zeroDateTimeBehavior=convertToNull&useUnicode=true"));
        String username = Preconditions.checkNotNull(System.getProperty(USERNAME_PROPERTY, "root"), USERNAME_PROPERTY);
        String password = Preconditions.checkNotNull(System.getProperty(PASSWORD_PROPERTY, "root"), PASSWORD_PROPERTY);

        Class.forName("com.mysql.jdbc.Driver");
        this.connection = DriverManager.getConnection(url, username, password);
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
            public ResultSet query(String sql) {
                Statement statement = null;
                try {
                    statement = connection.createStatement();
                    return statement.executeQuery(sql);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
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
