package org.activityinfo.test.ui;

import cucumber.api.DataTable;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.driver.TableDataParser;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests that dates are correctly perserved between client
 * and server.
 */
public class DateFormUiTest {


    public static final String DATABASE = "My Database";

    @Inject
    private UiApplicationDriver driver;

    @Test
    public void dataEntry() throws Exception {
        driver.login();
        driver.setup().createDatabase(name(DATABASE));
        driver.setup().addPartner("NRC", DATABASE);
        driver.setup().createForm(
                name("Form"),
                property("database", DATABASE),
                property("classicView", false));

        LocalDate date = new LocalDate(2015, 8, 6);
        
        List<FieldValue> fieldValues = new ArrayList<>();
        fieldValues.add(new FieldValue("partner", "NRC"));
        fieldValues.add(new FieldValue("Start Date", date.toString("YYYY-MM-dd")));
        fieldValues.add(new FieldValue("End Date", date.toString("YYYY-MM-dd")));
        driver.submitForm("Form", fieldValues);
        
        // Verify that dates are correctly recorded
        File exportedFile = driver.exportForm("Form");
        DataTable dataTable = TableDataParser.exportedDataTable(exportedFile);
        List<String> headers = dataTable.getGherkinRows().get(0).getCells();
        List<String> values = dataTable.getGherkinRows().get(1).getCells();
        
        assertThat(values.get(headers.indexOf("Date1")), equalTo(date.toString("M/d/YY")));
        assertThat(values.get(headers.indexOf("Date2")), equalTo(date.toString("M/d/YY")));
    }
}