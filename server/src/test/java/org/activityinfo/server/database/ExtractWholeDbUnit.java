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

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author yuriyz on 6/2/14.
 */
@SuppressWarnings({"NonJREEmulationClassesInClientCode", "AppEngineForbiddenCode"})
public class ExtractWholeDbUnit {

    public static final String XML_FILE_NAME = "sites-calculated-indicators.db.xml";
    public static final String DB_URL = "jdbc:mysql://localhost:3306/activityinfo?zeroDateTimeBehavior=convertToNull";
    public static final String DB_USERNAME = "root";
    public static final String DB_PW = "secret";

    /**
     * Utility to create a dbunit xml file from a local mysql database
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Connection jdbcConnection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PW);
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        String currentDir = System.getProperty("user.dir");
        String pathname = filepath(currentDir, "server", "src", "test", "resources", "dbunit", XML_FILE_NAME);

        System.out.println("Current directory: " + currentDir);
        System.out.println("File: " + pathname);

        File file = new File(pathname);
        if (!file.exists() && !file.createNewFile()) {
            throw new RuntimeException("Unable to create file: " + file.getAbsolutePath());
        }
        // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream(file, false));
    }

    public static String filepath(String... pathElements) {
        String result = "";
        for (String pathElement : pathElements) {
            result = result + pathElement + File.separatorChar;
        }
        return result.substring(0, result.length() - 1);
    }
}
