package org.activityinfo.test.steps.common;

import com.google.common.io.Files;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.pageobject.web.entry.HistoryEntry;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ScenarioScoped
public class DataEntrySteps {


    @Inject
    private ApplicationDriver driver;

    private File exportedFile = null;


    @Given("^I submit a \"([^\"]*)\" form with:$")
    public void I_have_submitted_a_form_with(String formName, List<FieldValue> values) throws Throwable {
        driver.submitForm(formName, values);
    }

    @Then("^the \"([^\"]*)\" form should have one submission$")
    public void the_form_should_have_one_submission(String formName) throws Throwable {
        the_form_should_have_submissions(formName, 1);
    }

    @Then("^the \"([^\"]*)\" form should have (\\d+) submissions$")
    public void the_form_should_have_submissions(String formName, int expectedCount) throws Throwable {
        int actualCount = driver.countFormSubmissions(formName);
        assertThat(actualCount, equalTo(expectedCount));
    }

    @When("^I export the form \"([^\"]*)\"$")
    public void I_export_the_form(String formName) throws Throwable {
        exportedFile = driver.exportForm(formName);
        System.out.println("Exported file: " + exportedFile.getAbsolutePath());

    }

    @When("^I update the submission with:$")
    public void I_update_the_submission_with(List<FieldValue> values) throws Throwable {
        driver.updateSubmission(values);
    }

    @Then("^the submission's history should show that I created it just now$")
    public void the_submission_s_history_should_show_that_I_created_it_just_now() throws Throwable {
        List<HistoryEntry> entries = driver.getSubmissionHistory();

        dumpChanges(entries);

        assertThat(entries, contains(createdToday()));
    }



    @Then("^the submission's history should show a change from (.*) to (.*)$")
    public void the_submission_s_history_should_show_one_change_from_to(String from, String to) throws Throwable {
        List<HistoryEntry> changes = driver.getSubmissionHistory();

        dumpChanges(changes);

        assertThat(changes.size(), greaterThan(1));
        assertThat(changes.get(0).getSummary(), Matchers.containsString("updated the entry"));
        assertThat(changes.get(0).getChanges().toString(), CoreMatchers.containsString(from));
        assertThat(changes.get(0).getChanges().toString(), CoreMatchers.containsString(to));

    }

    private void dumpChanges(List<HistoryEntry> entries) {
        StringBuilder s = new StringBuilder();
        for(HistoryEntry entry: entries) {
            entry.appendTo(s);
        }
        System.out.print(s);
        System.out.flush();
    }

    private Matcher<HistoryEntry> createdToday() {
        return hasProperty("summary", allOf(
                containsString("added the entry"),
                containsString(new LocalDate().toString("dd-MM-YYYY"))));
    }

    @Then("^the submission's detail shows:$")
    public void the_submission_s_detail_shows(List<FieldValue> values) throws Throwable {
        driver.getDetails().assertVisible(changeNamesToAlias(values));
    }

    private List<FieldValue> changeNamesToAlias(List<FieldValue> values) {
        for (FieldValue value : values) {
            value.setField(driver.getAliasTable().getAlias(value.getField()));
        }
        return values;
    }

    @Then("^the exported spreadsheet contains:$")
    public void the_exported_spreadsheet_should_contain(DataTable dataTable) throws Throwable {
        exportedDataTable(exportedFile).diff(dataTable);
    }


    private static DataTable exportedDataTable(File file) throws IOException, InvalidFormatException {

        byte[] bytes = Files.toByteArray(file);

        HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = workbook.getSheetAt(0);

        List<List<String>> rows = new ArrayList<>();

        // Find the number of columns 
        int numColumns = 0;
        for(int rowIndex=0;rowIndex<=sheet.getLastRowNum();++rowIndex) {
            numColumns = Math.max(numColumns, sheet.getRow(rowIndex).getLastCellNum());
        }

        // Create the table
        for(int rowIndex=0;rowIndex<=sheet.getLastRowNum();++rowIndex) {
            List<String> row = new ArrayList<>();
            Row excelRow = sheet.getRow(rowIndex);

            for(int colIndex=0;colIndex<numColumns;++colIndex) {
                row.add(toString(excelRow.getCell(colIndex)));
            }
            rows.add(row);
        }

        return DataTable.create(rows);
    }

    private static String toString(Cell cell) {
        if(cell == null) {
            return "";
        } else {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    return "";
                case Cell.CELL_TYPE_BOOLEAN:
                    return cell.getBooleanCellValue() ? "true" : "false";
                case Cell.CELL_TYPE_STRING:
                    return cell.getStringCellValue();
                case Cell.CELL_TYPE_NUMERIC:
                    return Double.toString(cell.getNumericCellValue());
            }
        }
        throw new UnsupportedOperationException("cellType: " + cell.getCellType());
    }

    public static void main(String[] args) throws IOException, InvalidFormatException {

        //File file = new File("/tmp/export4589625550230824900.xls");
        File file = new File("/home/alex/Downloads/ActivityInfo_Export_2015-05-01_235920.xls");
        System.out.println(exportedDataTable(file));

    }

}
