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
package org.activityinfo.test.ui;

import cucumber.api.DataTable;
import org.activityinfo.i18n.shared.I18N;
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
        
        assertThat(values.get(headers.indexOf(I18N.CONSTANTS.startDate())), equalTo(date.toString("M/d/YY")));
        assertThat(values.get(headers.indexOf(I18N.CONSTANTS.endDate())), equalTo(date.toString("M/d/YY")));
    }
}