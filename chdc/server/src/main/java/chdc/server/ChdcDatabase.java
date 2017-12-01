package chdc.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the connection with the SQL database
 */
public class ChdcDatabase {

    public static ChdcDatabase DATABASE = new ChdcDatabase();

    private static final Logger LOGGER = Logger.getLogger(ChdcDatabase.class.getName());

    private final HikariDataSource datasource;

    /**
     * Maintain one connection per thread, or one connection per request.
     */
    private final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

    private ChdcDatabase() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/chdc");
        config.setUsername("root");
        config.setPassword("root");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        datasource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DATABASE.threadLocalConnection.get();
        if(connection == null) {
            connection = DATABASE.datasource.getConnection();
            DATABASE.threadLocalConnection.set(connection);
        }

        connection.setAutoCommit(false);
        return connection;
    }

    public static void rollbackRequestTransactionIfActive() {
        Connection connection = DATABASE.threadLocalConnection.get();
        if(connection != null) {
            try {
                connection.rollback();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to rollback active transaction", e);
            }

            cleanupThreadConnection(connection);
        }
    }

    public static void commitRequestTransactionIfActive() {
        Connection connection = DATABASE.threadLocalConnection.get();
        if(connection != null) {
            try {
                connection.commit();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to commit active transaction", e);
            }
            cleanupThreadConnection(connection);
        }
    }

    private static void cleanupThreadConnection(Connection connection) {
        try {
            connection.close();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to release this request's connection", e);
        }

        DATABASE.threadLocalConnection.remove();
    }
}
