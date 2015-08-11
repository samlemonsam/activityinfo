package org.activityinfo.test.steps.common;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import gherkin.formatter.model.DataTableRow;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.driver.TableDataParser;
import org.activityinfo.test.pageobject.bootstrap.BsModal;
import org.activityinfo.test.pageobject.bootstrap.BsTable;
import org.activityinfo.test.pageobject.web.entry.HistoryEntry;
import org.activityinfo.test.pageobject.web.entry.TablePage;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import javax.inject.Inject;
import java.io.File;
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
    
    @Then("^I can submit a \"([^\"]*)\" form with:$")
    public void I_can_submit_a_form_with(String formName, List<FieldValue> values) throws Throwable {
        driver.submitForm(formName, values);
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
        assertTableUnorderedDiff(dataTable, TableDataParser.exportedDataTable(exportedFile));
    }

    @Then("^the exported csv contains:$")
    public void the_exported_csv_contains(DataTable dataTable) throws Throwable {
        assertTableUnorderedDiff(dataTable, TableDataParser.exportedDataTableFromCsvFile(exportedFile));
    }

    public void assertTableUnorderedDiff(DataTable dataTable, DataTable fileTable) {
        List<String> expectedColumns = dataTable.getGherkinRows().get(0).getCells();

        DataTable excelTable = driver.getAliasTable().deAlias(fileTable);
        DataTable subsettedExcelTable = subsetColumns(excelTable, expectedColumns);

        subsettedExcelTable.unorderedDiff(dataTable);
    }

    @When("^I export the schema of \"([^\"]*)\" database$")
    public void I_export_the_schema_of_database(String databaseName) throws Throwable {
        exportedFile = driver.setup().exportDatabaseSchema(databaseName);
    }

    private static DataTable subsetColumns(DataTable table, List<String> expectedColumns) {
        
        List<String> columns = table.getGherkinRows().get(0).getCells();
        List<Integer> columnIndexes = new ArrayList<>();
        List<String> missingColumns = new ArrayList<>();
        for(String expectedColumn : expectedColumns) {
            int index = columns.indexOf(expectedColumn);
            if(index == -1) {
                missingColumns.add(expectedColumn);
            } else {
                columnIndexes.add(index);
            }
        }
        
        if(!missingColumns.isEmpty()) {
            throw new AssertionError("Missing expected columns " + missingColumns + " in table:\n" + table.toString());
        }
        
        List<List<String>> rows = new ArrayList<>();
        for (DataTableRow dataTableRow : table.getGherkinRows()) {
            List<String> row = new ArrayList<>();
            for (Integer columnIndex : columnIndexes) {
                row.add(dataTableRow.getCells().get(columnIndex));
            }
            rows.add(row);
        }
        
        return DataTable.create(rows);
    }

    @Then("^submissions for \"([^\"]*)\" form are:$")
    public void submissions_for_form_are(String formName, DataTable dataTable) throws Throwable {
        driver.assertDataEntryTableForForm(formName, dataTable);
    }

    @Then("^\"([^\"]*)\" database entry appears with lock in Data Entry and cannot be modified nor deleted with any of these values:$")
    public void database_entry_appears_with_lock_in_Data_Entry_and_cannot_be_modified_nor_deleted_with(String databaseName, List<FieldValue> values) throws Throwable {
        driver.assertEntryCannotBeModifiedOrDeleted(databaseName, values);
    }

    @Then("^new entry with end date \"([^\"]*)\" cannot be submitted in \"([^\"]*)\" form$")
    public void new_entry_with_end_date_cannot_be_submitted_in_database(String endDate, String formName) throws Throwable {
        driver.assertSubmissionIsNotAllowedBecauseOfLock(formName, endDate);
    }

    @Then("^\"([^\"]*)\" form entry appears with lock in Data Entry and cannot be modified nor deleted with any of these values:$")
    public void form_entry_appears_with_lock_in_Data_Entry_and_cannot_be_modified_nor_deleted_with_any_of_these_values(String formName, List<FieldValue> values) throws Throwable {
        driver.assertEntryCannotBeModifiedOrDeleted(formName, values);
    }

    @When("^I open a new form submission for \"([^\"]*)\" then following fields are visible:$")
    public void I_open_a_new_form_submission_for_then_following_fields_are_visible(String formName, List<String> fieldLabels) throws Throwable {
        driver.assertFieldsOnNewForm(formName, fieldLabels);
    }

    @When("^I open a new form submission for \"([^\"]*)\" then field values are:$")
    public void I_open_a_new_form_submission_for_then_field_values_are(String formName, List<FieldValue> values) throws Throwable {
        driver.assertFieldValuesOnNewForm(formName, values);
    }

    @When("^edit entry in new table with field name \"([^\"]*)\" and value \"([^\"]*)\" in the database \"([^\"]*)\" in the form \"([^\"]*)\" with:$")
    public void edit_entry_in_new_table_with_field_name_and_value_in_the_database_in_the_form_with(String fieldName, String fieldValue,
                                                                                                   String database, String formName,
                                                                                                   List<FieldValue> fieldValues
    ) throws Throwable {

        TablePage tablePage = openFormTable(database, formName);
        tablePage.table().showAllColumns().waitUntilColumnShown(driver.getAliasTable().getAlias(fieldName));
        tablePage.table().waitForCellByText(fieldValue).getContainer().clickWhenReady();

        BsModal bsModal = tablePage.table().editSubmission();
        bsModal.fill(driver.getAliasTable().alias(fieldValues)).click(I18N.CONSTANTS.save()).waitUntilClosed();
    }

    @Then("^table has rows:$")
    public void table_has_rows(DataTable dataTable) throws Throwable {
        assertHasRows(dataTable, false);
    }

    @Then("^table has rows with hidden built-in columns:$")
    public void table_has_rows_with_hidden_built_in_columns(DataTable dataTable) throws Throwable {
        assertHasRows(dataTable, true);
    }

    private void assertHasRows(DataTable dataTable, boolean hideBuiltInColumns) throws Throwable {
        BsTable table = driver.tablePage().table();
        if (hideBuiltInColumns) {
            table.hideBuiltInColumns();
        }
        table.waitUntilAtLeastOneRowIsLoaded().assertRowsPresent(dataTable);
    }

    @And("^filter column \"([^\"]*)\" with:$")
    public void filter_column_with(String columnName, List<String> filterValues) throws Throwable {
        columnName = driver.getAliasTable().getAlias(columnName);

        BsTable table = driver.tablePage().table().showAllColumns().waitUntilAtLeastOneRowIsLoaded();
        table.filter(columnName).select(filterValues).apply();
    }

    @When("^open table for the \"([^\"]*)\" form in the database \"([^\"]*)\"$")
    public void open_table_for_the_form_in_the_database(String formName, String databaseName) throws Throwable {
        openFormTable(databaseName, formName);
    }

    private TablePage openFormTable(String databaseName, String formName) {
        databaseName = driver.getAliasTable().getAlias(databaseName);
        formName = driver.getAliasTable().getAlias(formName);

        return driver.openFormTable(databaseName, formName);
    }

    @And("^filter date column \"([^\"]*)\" with start date \"([^\"]*)\" and end date \"([^\"]*)\":$")
    public void filter_date_column_with_start_date_and_end_date_(String columnName, String startDate, String endDate) throws Throwable {
        columnName = driver.getAliasTable().getAlias(columnName);

        BsTable table = driver.tablePage().table().showAllColumns().waitUntilAtLeastOneRowIsLoaded();
        table.filter(columnName).fillRange(LocalDate.parse(startDate), LocalDate.parse(endDate)).apply();
    }

    @And("^delete rows with text:$")
    public void delete_rows_with_text(List<String> cells) throws Throwable {
        driver.removeRows(cells);
    }
}
