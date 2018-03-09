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
package org.activityinfo.ui.client.component.importDialog;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.mysql.MySqlConnection;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;


@SuppressWarnings({"NonJREEmulationClassesInClientCode", "AppEngineForbiddenCode"})
public class ExtractDbUnit {

    /**
     * Utility to create a dbunit xml file from a local mysql database
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Connection jdbcConnection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/activityinfo?zeroDateTimeBehavior=convertToNull", "root", "root");
        IDatabaseConnection connection = new MySqlConnection(jdbcConnection, null);

        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);
//
//        partialDataSet.addTable("userlogin", "select * from userlogin where userid in " +
//                "(select owneruserid from userdatabase where databaseid=1100)");

        int databaseId = 4;
        
        partialDataSet.addTable("country", "select * from country where countryid in (select countryId from userdatabase where databaseid=" + databaseId + ")");
        partialDataSet.addTable("locationtype", "select * from locationtype where locationTypeId=1");
        partialDataSet.addTable("location", "select * from location where locationtypeid = 1");
        partialDataSet.addTable("userdatabase", "select * from userdatabase where databaseid=" + databaseId + "");
        partialDataSet.addTable("partnerindatabase", "select * from partnerindatabase where databaseid=" + databaseId + "");
        partialDataSet.addTable("partner", "select * from partner where partnerid in (select partnerid from partnerindatabase where databaseid=" + databaseId + ")");
        partialDataSet.addTable("activity", "select * from activity where activityId=33");
        partialDataSet.addTable("indicator", "select * from indicator where activityId=33");
        partialDataSet.addTable("attributegroupinactivity", "select * from attributegroupinactivity where activityid = 33");
        partialDataSet.addTable("attributegroup", "select * from attributegroup where attributegroupid in" +
                " (select attributegroupid from attributegroupinactivity where activityId=33)");
        partialDataSet.addTable("attribute", "select * from attribute where attributegroupid in" +
                " (select attributegroupid from attributegroupinactivity where activityId=33)");
        partialDataSet.addTable("userlogin", "select * from userlogin where userid in " +
                "(select userId from sitehistory where siteid in (302007470, 968196924, 1379807452, 1435486740))");

        partialDataSet.addTable("site", "select * from site where siteid in (302007470, 968196924, 1379807452, 1435486740)");
        partialDataSet.addTable("sitehistory", "select * from sitehistory where siteid in (302007470, 968196924, 1379807452, 1435486740)");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("store/mysql/src/test/resources/org/activityinfo/store/mysql/history.db.xml"));
    }
}
