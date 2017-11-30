package chdc.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Handles the connection with the SQL database
 */
public class ChdcDatabase implements ConnectionProvider {

    public static ChdcDatabase DATABASE = new ChdcDatabase();

    private final HikariDataSource datasource;

    /**
     * Maintain one connection per thread, or one connection per request.
     */
    private final ThreadLocal<Connection> connection = new ThreadLocal<>();

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


    public static DSLContext sql() {
        return DSL.using(DATABASE, SQLDialect.MYSQL);
    }

    @Override
    public Connection acquire() throws DataAccessException {
        Connection threadConnection = this.connection.get();
        if(threadConnection == null) {
            try {
                threadConnection = datasource.getConnection();
                connection.set(threadConnection);
            } catch (SQLException e) {
                throw new DataAccessException("Failed to acquire connection", e);
            }
        }
        return threadConnection;
    }

    @Override
    public void release(Connection connection) throws DataAccessException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("Exception closing connection", e);
        }
    }
}
