package org.activityinfo.ui.client.importer;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

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
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);
//
//        partialDataSet.addTable("userlogin", "select * from userlogin where userid in " +
//                "(select owneruserid from userdatabase where databaseid=1100)");

        partialDataSet.addTable("country", "select * from country where countryid=291");
        partialDataSet.addTable("locationtype", "select * from locationtype where locationtypeid = 50573");
        partialDataSet.addTable("location", "select * from location where locationtypeid = 50573");
        partialDataSet.addTable("userdatabase", "select * from userdatabase where databaseid=1470");
        partialDataSet.addTable("partnerindatabase", "select * from partnerindatabase where databaseid=1470");
        partialDataSet.addTable("partner", "select * from partner where partnerid in (select partnerid from partnerindatabase where databaseid=1470)");
        partialDataSet.addTable("activity", "select * from activity where activityId=11218");
        partialDataSet.addTable("indicator", "select * from indicator where activityId=11218");
        partialDataSet.addTable("attributegroupinactivity", "select * from attributegroupinactivity where activityId=11218");
        partialDataSet.addTable("attributegroup", "select * from attributegroup where attributegroupid in" +
                " (select attributegroupid from attributegroupinactivity where activityId=11218)");
        partialDataSet.addTable("attribute", "select * from attribute where attributegroupid in" +
                " (select attributegroupid from attributegroupinactivity where activityId=11218)");

        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src/test/resources/dbunit/chad-form.db.xml"));

    }
}
