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

import com.google.appengine.tools.mapreduce.InputReader;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads the list of sites
 */
class SiteReader extends InputReader<Integer> {

    private static final Logger LOGGER = Logger.getLogger(SiteReader.class.getName());
    private final int activityId;

    private transient MySqlQueryExecutor executor;
    private transient ResultSet resultSet;

    private int lastId = 0;

    public SiteReader(int activityId) {
        this.activityId = activityId;
    }

    @Override
    public void beginSlice() throws IOException {
        super.beginSlice();

        LOGGER.info("Starting site slice at " + lastId);

        try {
            executor = new MySqlQueryExecutor();
        } catch (SQLException e) {
            throw new IOException("Failed to open connection to MySQL", e);
        }
        resultSet = executor.query("SELECT s.siteId FROM site s " +
                        "WHERE s.deleted = 0 AND s.siteId > ? AND s.activityId = ? " +
                        "ORDER BY s.siteId",
                lastId, activityId);
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
