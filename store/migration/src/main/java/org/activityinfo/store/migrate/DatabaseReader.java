package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.InputReader;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads the list of user databases
 */
class DatabaseReader extends InputReader<Integer> {

    private static final Logger LOGGER = Logger.getLogger(DatabaseReader.class.getName());

    private transient MySqlQueryExecutor executor;
    private transient ResultSet resultSet;

    private int lastId = 0;

    @Override
    public void beginSlice() throws IOException {
        super.beginSlice();

        LOGGER.info("Starting database slice at " + lastId);

        try {
            executor = new MySqlQueryExecutor();
        } catch (SQLException e) {
            throw new IOException("Failed to open connection to MySQL", e);
        }
        resultSet = executor.query("SELECT databaseId FROM userdatabase WHERE dateDeleted IS NULL AND databaseId > ?", lastId);
    }

    @Override
    public Integer next() throws IOException, NoSuchElementException {
        boolean hasNext;
        try {
            hasNext = resultSet.next();
        } catch (SQLException e) {
            throw new IOException(e);
        }
        if(!hasNext) {
            throw new NoSuchElementException();
        }
        try {
            lastId = resultSet.getInt(1);
            return lastId;
        } catch (SQLException e) {
            throw new IOException(e);
        }

    }

    @Override
    public void endSlice() throws IOException {
        LOGGER.info("End database slice at " + lastId);

        if(resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to close result set", e);
            }
        }
        if(executor != null) {
            executor.close();
        }
    }
}
