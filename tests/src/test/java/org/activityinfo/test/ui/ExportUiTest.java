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

import org.activityinfo.test.driver.MonthlyFieldValue;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ExportUiTest {


    public static final String WASH_DATABASE = "LCPR-R WASH";
    public static final String WASH_SITE_FORM = "Site";
    public static final String INDICATOR_NAME = "# of Displaced Syrians with improved water supply";
    
    @Inject
    private UiApplicationDriver driver;
    
    
    @Test
    public void largeDatabase() throws Exception {

        driver.login();
        driver.setup().createDatabase(name(WASH_DATABASE));
        driver.setup().addPartner("ACF", WASH_DATABASE);

        driver.setup().createForm(name(WASH_SITE_FORM),
                property("database", WASH_DATABASE),
                property("reportingFrequency", "monthly"));
        
        driver.setup().createField(name(INDICATOR_NAME), 
                property("form", WASH_SITE_FORM), 
                property("type", "quantity"));


        // Submit 200 sites with 6 months worth of data each
        double expectedTotal = 0;
        for(int i=0;i<200;i++) {
            List<MonthlyFieldValue> fieldValues = new ArrayList<>();
            for(int month=1;month<6;++month) {
                int count = month * 10;
                expectedTotal += count;
                
                MonthlyFieldValue fieldValue = new MonthlyFieldValue();
                fieldValue.setYear(2015);
                fieldValue.setMonth(month);
                fieldValue.setField(INDICATOR_NAME);
                fieldValue.setValue(count);
                fieldValues.add(fieldValue);
            }
            driver.setup().submitForm(WASH_SITE_FORM, "ACF", fieldValues);
        }

        File file = driver.setup().exportForm(WASH_SITE_FORM);
        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
        HSSFSheet worksheet = workbook.getSheetAt(0);

        // Find indicator column
        double exportedTotal = 0;
        int indicatorColumn = findColumn(worksheet);
        for(int rowIndex=2;rowIndex<=worksheet.getLastRowNum();++rowIndex) {
            HSSFRow row = worksheet.getRow(rowIndex);
            HSSFCell cell = row.getCell(indicatorColumn);
            exportedTotal += cell.getNumericCellValue();
        }
        assertThat(exportedTotal, equalTo(expectedTotal));
    }

    private int findColumn(HSSFSheet worksheet) {
        HSSFRow headerRow = worksheet.getRow(1);
        for(int i=0;i< headerRow.getLastCellNum();++i) {
            if(headerRow.getCell(i).getStringCellValue().equals(driver.getAliasTable().getAlias(INDICATOR_NAME))) {
                return i;
            }
        }
        throw new AssertionError("Could not find column named " + driver.getAliasTable().getAlias(INDICATOR_NAME));
    }

}
