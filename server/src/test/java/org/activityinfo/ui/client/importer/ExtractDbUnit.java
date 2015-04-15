package org.activityinfo.ui.client.importer;

import org.dbunit.database.DatabaseConnection;
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
        partialDataSet.addTable("locationtype", "select * from locationtype where countryid in (select countryId from userdatabase where databaseid=" + databaseId + ")");
        partialDataSet.addTable("location", "select * from location where locationtypeid in (select locationtypeid from locationtype where countryid in (select countryId from userdatabase where databaseid=" + databaseId + "))");
        partialDataSet.addTable("userdatabase", "select * from userdatabase where databaseid=" + databaseId + "");
        partialDataSet.addTable("partnerindatabase", "select * from partnerindatabase where databaseid=" + databaseId + "");
        partialDataSet.addTable("partner", "select * from partner where partnerid in (select partnerid from partnerindatabase where databaseid=" + databaseId + ")");
        partialDataSet.addTable("activity", "select * from activity where databaseId=" + databaseId + "");
        partialDataSet.addTable("indicator", "select * from indicator where activityId in (select activityid from activity where databaseid=" + databaseId + ")");
        partialDataSet.addTable("attributegroupinactivity", "select * from attributegroupinactivity where activityid in (select activityid from activity where databaseid=" + databaseId + ")");
        partialDataSet.addTable("attributegroup", "select * from attributegroup where attributegroupid in" +
                " (select attributegroupid from attributegroupinactivity where activityId in (select activityid from activity where databaseid=" + databaseId + "))");
        partialDataSet.addTable("attribute", "select * from attribute where attributegroupid in" +
                " (select attributegroupid from attributegroupinactivity where activityId in (select activityid from activity where databaseid=" + databaseId + "))");       
        partialDataSet.addTable("site", "select * from site where activityid in (select activityid from activity where databaseid=" + databaseId + ")");
        partialDataSet.addTable("attributevalue", "select * from attributevalue where siteid in (select siteid from site where activityid in (select activityid from activity where databaseid=" + databaseId + "))");
        partialDataSet.addTable("reportingperiod", "select * from reportingperiod where siteid in (select siteid from site where activityid in (select activityid from activity where databaseid=" + databaseId + "))");
        partialDataSet.addTable("indicatorvalue", "select * from  indicatorvalue where reportingperiodid in (select reportingperiodid from reportingperiod where siteid in (select siteid from site where activityid in (select activityid from activity where databaseid=" + databaseId + ")))");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("store/mysql/src/test/resources/org/activityinfo/store/mysql/rdc.db.xml"));

    }
}
