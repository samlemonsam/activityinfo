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
package org.activityinfo.test.steps.common;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import gherkin.formatter.model.DataTableRow;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.test.Sleep;
import org.activityinfo.test.driver.*;
import org.activityinfo.test.pageobject.bootstrap.*;
import org.activityinfo.test.pageobject.web.entry.DataEntryTab;
import org.activityinfo.test.pageobject.web.entry.HistoryEntry;
import org.activityinfo.test.pageobject.web.entry.TablePage;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.UserAccount;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@ScenarioScoped
public class DataEntrySteps {

    public static final Logger LOGGER = Logger.getLogger(DataEntrySteps.class.getName());

    @Inject
    private ApplicationDriver driver;

    @Inject
    private Accounts accounts;

    private File exportedFile = null;

    private ResourceId lastSubmissionId = null;

    public static final String MISSING_VALUE = "<Missing>";

    @Given("^I submit a \"([^\"]*)\" form with:$")
    public void I_have_submitted_a_form_with(String formName, List<FieldValue> values) throws Throwable {
        lastSubmissionId = driver.submitForm(formName, values);
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
        for (HistoryEntry entry : entries) {
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
        for (String expectedColumn : expectedColumns) {
            int index = columns.indexOf(expectedColumn);
            if (index == -1) {
                missingColumns.add(expectedColumn);
            } else {
                columnIndexes.add(index);
            }
        }

        if (!missingColumns.isEmpty()) {
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

    @Then("^\"([^\"]*)\" form entry appears with lock in Data Entry and cannot be modified nor deleted with any of these values:$")
    public void form_entry_appears_with_lock_in_Data_Entry_and_cannot_be_modified_nor_deleted_with_any_of_these_values(String formName, List<FieldValue> values) throws Throwable {
        driver.assertEntryCannotBeModifiedOrDeleted(formName, values);
    }

    @When("^I open a new form submission for \"([^\"]*)\" then following fields are visible:$")
    public void I_open_a_new_form_submission_for_then_following_fields_are_visible(String formName, List<String> fieldLabels) throws Throwable {
        driver.assertFieldsOnNewForm(formName, fieldLabels);
    }

    @When("^I open a new form submission for \"([^\"]*)\" then following fields are invisible:$")
    public void I_open_a_new_form_submission_for_then_following_fields_are_invisible(String formName, List<String> fieldLabels) throws Throwable {

        for (String fieldLabel : fieldLabels) {
            Optional<BsFormPanel.BsField> formItem = driver.getFormFieldFromNewSubmission(formName, fieldLabel);
            assertTrue(!formItem.isPresent());
        }
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
        tablePage.table().showAllColumns();//.waitUntilColumnShown(driver.getAliasTable().getAlias(fieldName));
        tablePage.table().waitForCellByText(fieldValue).getContainer().clickWhenReady();

        BsModal bsModal = tablePage.table().editSubmission();
        bsModal.fill(driver.getAliasTable().alias(fieldValues))
                .save();
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
        table.showAllColumns();
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

    @Given("^I have submitted to \"([^\"]*)\" form table in \"([^\"]*)\" database:$")
    public void I_have_submitted_to_form_table_in_database(String formName, String database, DataTable dataTable) throws Throwable {
        TablePage tablePage = driver.openFormTable(driver.getAliasTable().getAlias(database), driver.getAliasTable().getAlias(formName));
        BsModal modal = tablePage.table().newSubmission();

        modal.fill(dataTable, driver.getAliasTable());

        modal.click(I18N.CONSTANTS.save()).waitUntilClosed();
    }

    @Then("^\"([^\"]*)\" filter for \"([^\"]*)\" form has values:$")
    public void filter_for_form_has_values(String filterName, String formName, List<String> filterValues) throws Throwable {
        List<String> filterItemsOnUi = driver.getFilterValues(filterName, formName);

        filterItemsOnUi = driver.getAliasTable().deAlias(filterItemsOnUi);
        filterItemsOnUi.removeAll(filterValues);

        assertTrue(filterItemsOnUi.isEmpty());
    }

    @When("^I import into the form \"([^\"]*)\" spreadsheet:$")
    public void I_import_into_the_form_spreadsheet(String formName, DataTable dataTable) throws Throwable {
        driver.importForm(formName, dataTable);
    }

    @When("^I import into the form \"([^\"]*)\" spreadsheet with (\\d+) rows:$")
    public void I_import_into_the_form_spreadsheet_with_rows(String formName, int quantityOfRowCopy, DataTable dataTable) throws Throwable {
        driver.importRowIntoForm(formName, dataTable, quantityOfRowCopy);
    }

    @Then("^\"([^\"]*)\" table has (\\d+) rows in \"([^\"]*)\" database$")
    public void table_has_rows_in_database(String formName, int numberOfExpectedRows, String database) throws Throwable {
        TablePage tablePage = driver.openFormTable(driver.getAliasTable().getAlias(database), driver.getAliasTable().getAlias(formName));
        BsTable.waitUntilRowsLoaded(tablePage, numberOfExpectedRows);
    }

    @Then("^new form dialog for \"([^\"]*)\" form has following items for partner field$")
    public void new_form_dialog_for_form_has_following_items_for_partner_field(String formName, List<String> expectedPartnerValues) throws Throwable {
        DataEntryDriver dataEntryDriver = driver.startNewSubmission(formName);
        boolean foundPartnerField = false;
        while (dataEntryDriver.nextField()) {
            switch (dataEntryDriver.getLabel()) {
                case "Partner":
                    foundPartnerField = true;
                    List<String> items = Lists.newArrayList(dataEntryDriver.availableValues());

                    for (String expected : expectedPartnerValues) {
                        items.remove(expected);
                        items.remove(driver.getAliasTable().getAlias(expected));
                    }
                    assertTrue(items.isEmpty());
                    break;
            }
        }

        dataEntryDriver.close();
        if (!foundPartnerField) {
            throw new RuntimeException("Failed to find partner field.");
        }
    }

    @Then("^old table for \"([^\"]*)\" form shows:$")
    public void old_table_for_form_shows(String formName, DataTable dataTable) throws Throwable {
        DataTable uiTable = driver.getAliasTable().deAlias(driver.oldTable(formName));
        dataTable.unorderedDiff(uiTable);
    }

    @Then("^\"([^\"]*)\" field contains image.$")
    public void field_contains_image(String imageFieldName) throws Throwable {
        DataEntryTab dataEntryTab = (DataEntryTab) driver.getCurrentPage();

        dataEntryTab.selectSubmission(0);
        BsModal modal = dataEntryTab.editBetaSubmission();

        Sleep.sleepSeconds(35); // give it a time fetch image serving url and put it to img.src
        assertTrue(modal.form().findFieldByLabel(driver.getAliasTable().getAlias(imageFieldName)).isBlobImageLoaded());

        modal.cancel();
    }

    private String firstDownloadableLinkOfFirstSubmission(String attachmentFieldName) {
        DataEntryTab dataEntryTab = (DataEntryTab) driver.getCurrentPage();

        dataEntryTab.selectSubmission(0);
        BsModal modal = dataEntryTab.editBetaSubmission();

        String blobLink = modal.form().findFieldByLabel(driver.getAliasTable().getAlias(attachmentFieldName)).getFirstBlobLink();

        assertTrue(blobLink.startsWith("https")); // all links must start from https

        modal.cancel();

        return blobLink;
    }

    @Then("^\"([^\"]*)\" field has downloadable link.$")
    public void field_has_downloadable_link(String attachmentFieldName) throws Throwable {
        String blobLink = firstDownloadableLinkOfFirstSubmission(attachmentFieldName);

        UserAccount currentUser = driver.setup().getCurrentUser();

        Client client = new Client();
        client.addFilter(new HTTPBasicAuthFilter(currentUser.getEmail(), currentUser.getPassword()));
        client.setFollowRedirects(false);

        ClientResponse clientResponse = client.resource(blobLink).get(ClientResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), clientResponse.getStatus());
    }

    @Then("^\"([^\"]*)\" field's downloadable link is forbidden for anonymous access.$")
    public void field_downloadable_link_is_forbidden_for_anonymous_access(String attachmentFieldName) throws Throwable {
        String blobLink = firstDownloadableLinkOfFirstSubmission(attachmentFieldName);

        URL url = new URL(blobLink);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), connection.getResponseCode());
    }

    @Then("^\"([^\"]*)\" field's downloadable link is forbidden for \"([^\"]*)\"$")
    public void field_s_downloadable_link_is_forbidden_for(String attachmentFieldName, String userEmail) throws Throwable {

        UserAccount account = accounts.ensureAccountExists(userEmail);
        String blobLink = firstDownloadableLinkOfFirstSubmission(attachmentFieldName);

        Client client = new Client();
        client.addFilter(new HTTPBasicAuthFilter(account.getEmail(), account.getPassword()));
        client.setFollowRedirects(false);

        ClientResponse clientResponse = client.resource(blobLink).get(ClientResponse.class);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), clientResponse.getStatus());
    }

    @When("^I begin a new submission for \"([^\"]*)\"$")
    public void I_begin_a_new_submission_for(String formName) throws Throwable {
        driver.beginNewFormSubmission(formName);
    }

    @And("^I enter:$")
    public void I_enter(List<FieldValue> fieldValues) throws Throwable {
        Preconditions.checkNotNull(driver.getCurrentModal());

        BsModal modal = driver.getCurrentModal();
        driver.getAliasTable().alias(fieldValues);

        for (FieldValue fieldValue : fieldValues) {
            modal.form().findFieldByLabel(fieldValue.getField()).fill(fieldValue.getValue(), fieldValue.getControlType());
        }
    }

    @Then("^the \"([^\"]*)\" field should be enabled$")
    public void the_field_should_be_enabled(String fieldLabel) throws Throwable {
        assertTrue(getField(driver.getAliasTable().getAlias(fieldLabel)).isEnabled());
    }

    @Then("^the \"([^\"]*)\" field should be disabled$")
    public void the_field_should_be_disabled(String fieldLabel) throws Throwable {
        assertFalse(getField(driver.getAliasTable().getAlias(fieldLabel)).isEnabled());
    }

    private BsFormPanel.BsField getField(String fieldLabel) {
        BsFormPanel.BsField field = driver.getCurrentModal().form().findFieldByLabel(fieldLabel);
        assertNotNull(field);
        return field;
    }

    @When("^I save the submission$")
    public void I_save_the_submission() throws Throwable {
        BsModal modal = driver.getCurrentModal();
        modal.click(I18N.CONSTANTS.save());
        modal.waitUntilClosed();
    }

    @And("^I edit first row$")
    public void I_edit_first_row() throws Throwable {
        TablePage tablePage = (TablePage) driver.getCurrentPage();
        BsModal modal = tablePage.table().waitUntilAtLeastOneRowIsLoaded().selectFirstRow().editSubmission();
        driver.setCurrentModal(modal);
    }

    @Then("^\"([^\"]*)\" field should be with an empty value$")
    public void field_should_be_with_an_empty_value(String fieldLabel) throws Throwable {
        assertTrue(driver.getCurrentModal().form().findFieldByLabel(driver.getAliasTable().getAlias(fieldLabel)).isEmpty());
    }

    @Then("^the value of \"([^\"]*)\" in the submission should be ([^\\s]+)$")
    public void theValueOfInTheSubmissionShouldBeResult(String fieldName, String result) throws Throwable {
        Preconditions.checkArgument(lastSubmissionId != null, "No current submission");

        List<FieldValue> values = driver.getFieldValues(lastSubmissionId);

        if(MISSING_VALUE.equalsIgnoreCase(result)) {
            assertMissingValue(values, fieldName);
            return;
        }


        FieldValue value = find(values, fieldName);
        // Try comparing numbers
        try {
            double expected = Double.parseDouble(result);
            double actual = Double.parseDouble(value.getValue());
            if(expected != actual) {
                throw new AssertionError(String.format("Expected value %f for field '%s', found %f",
                        expected, fieldName, actual));
            }
        } catch (NumberFormatException e) {
            throw new UnsupportedEncodingException("TODO: implement non-numeric comparisons");
        }
    }

    private void assertMissingValue(List<FieldValue> values, String fieldName) {
        for (FieldValue value : values) {
            if(value.getField().equals(fieldName)) {
                throw new AssertionError("Expected missing value for " + fieldName + ", found: " + value.getValue());
            }
        }
    }

    private FieldValue find(List<FieldValue> values, String fieldName) {
        for (FieldValue value : values) {
            if(value.getField().equals(fieldName)) {
                return value;
            }
        }
        throw new AssertionError("No field with name '" + fieldName + "'. Found: " + values);
    }

    @And("^I open a new form submission on table page$")
    public void I_open_a_new_form_submission_on_table_page() throws Throwable {
        driver.tablePage().table().newSubmission();
    }

    @And("^I enter \"([^\"]*)\" repeating subform values:$")
    public void I_enter_repeating_subform_values(String repeatingSubformName, DataTable subformValues) throws Throwable {
        BsModal modal = currentOpenedForm();

        DataTableRow header = subformValues.getGherkinRows().get(0);

        addRepetitiveFormsIfNeeded(repeatingSubformName, modal, subformValues.getGherkinRows().size() - 2);

        for (int i = 2; i < subformValues.getGherkinRows().size(); i++) {
            DataTableRow row = subformValues.getGherkinRows().get(i);
            for (int j = 0; j < header.getCells().size(); j++) {
                String label = driver.getAliasTable().getAlias(header.getCells().get(j));
                String value = row.getCells().get(j);

                LOGGER.finest("entering repeating subform values: label: " + label + ", value:" + value);

                modal.form().findFieldsByLabel(label).get(i - 2).fill(value);
                Sleep.sleepMillis(100);
            }
        }
    }

    private void addRepetitiveFormsIfNeeded(String subformName, BsModal modal, int numberOfRequiredForms) {
        SubformContainer subform = modal.subform(subformName);
        int numberOfRepetitiveForms = subform.getRepeatingPanelsCount();

        for (int i = numberOfRepetitiveForms; i < numberOfRequiredForms; i++) {
            subform.addAnother();
            Sleep.sleepMillis(400);
        }

        assertEquals(numberOfRequiredForms, subform.getRepeatingPanelsCount());

    }

    @And("^I save submission$")
    public void I_save_submission() throws Throwable {
        currentOpenedForm().save();
    }

    @And("^I enter values:$")
    public void I_enter_values(DataTable table) throws Throwable {
        BsModal modal = currentOpenedForm();

        DataTableRow header = table.getGherkinRows().get(0);
        DataTableRow type = table.getGherkinRows().get(1);

        for (int i = 2; i < table.getGherkinRows().size(); i++) {
            DataTableRow row = table.getGherkinRows().get(i);
            for (int j = 0; j < row.getCells().size(); j++) {

                String label = header.getCells().get(j);
                if (!BsModal.isBuiltinLabel(label)) {
                    label = driver.getAliasTable().getAlias(label);
                }

                String value = row.getCells().get(j);
                if (label.equalsIgnoreCase(I18N.CONSTANTS.partner())) {
                    value = driver.getAliasTable().getAlias(value);
                }

                modal.form().findFieldByLabel(label).fill(value, type.getCells().get(j));
                Sleep.sleepMillis(100);
            }
        }
    }

    private BsModal currentOpenedForm() {
        return BsModal.find(driver.tablePage().getPage().root());
    }

    @And("^open edit dialog for entry in new table with field value \"([^\"]*)\"$")
    public void open_edit_dialog_for_entry_in_new_table_with_field_name_and_value(String fieldValue) throws Throwable {
        TablePage tablePage = driver.tablePage();
        tablePage.table().showAllColumns();//.waitUntilColumnShown(driver.getAliasTable().getAlias(fieldName));
        tablePage.table().waitForCellByText(fieldValue).getContainer().clickWhenReady();

        tablePage.table().editSubmission();
    }

    @Then("^opened form has repeating subform values:$")
    public void opened_form_has_repeating_subform_values(DataTable expectedSubformValues) throws Throwable {
        BsModal modal = currentOpenedForm();

        DataTableRow header = expectedSubformValues.getGherkinRows().get(0);
        DataTableRow type = expectedSubformValues.getGherkinRows().get(1);

        for (int i = 2; i < expectedSubformValues.getGherkinRows().size(); i++) {
            DataTableRow row = expectedSubformValues.getGherkinRows().get(i);
            for (int j = 0; j < header.getCells().size(); j++) {
                String label = driver.getAliasTable().getAlias(header.getCells().get(j));
                String currentValue = modal.form().findFieldsByLabel(label).get(i - 2).getValue(ControlType.fromValue(type.getCells().get(j)));
                assertEquals(row.getCells().get(j), currentValue);
            }
        }
    }

    @And("^delete item (\\d+) of \"([^\"]*)\" repeating subform$")
    public void delete_item_of_repeating_subform(int itemIndex, String subformName) throws Throwable {
        BsModal modal = currentOpenedForm();

        List<SubformPanel> panels = modal.subform(subformName).getPanels();
        panels.get(itemIndex - 1).delete();

    }

    @And("^I enter \"([^\"]*)\" subform values:$")
    public void I_enter_subform_values(String subformName, DataTable subformValues) throws Throwable {
        BsModal modal = currentOpenedForm();

        DataTableRow header = subformValues.getGherkinRows().get(0);

        SubformContainer subform = modal.subform(subformName);

        for (int i = 2; i < subformValues.getGherkinRows().size(); i++) {
            DataTableRow row = subformValues.getGherkinRows().get(i);

            String keyLabel = row.getCells().get(0);
            subform.selectKey(keyLabel);

            for (int j = 1; j < header.getCells().size(); j++) {
                String label = driver.getAliasTable().getAlias(header.getCells().get(j));

                modal.form().findFieldByLabel(label).fill(row.getCells().get(j));
            }
        }
    }

    @Then("^opened form has \"([^\"]*)\" subform keyed values:$")
    public void opened_form_has_subform_keyed_values(String subformName, DataTable expectedSubformValues) throws Throwable {
        BsModal modal = currentOpenedForm();

        DataTableRow header = expectedSubformValues.getGherkinRows().get(0);
        DataTableRow type = expectedSubformValues.getGherkinRows().get(1);

        SubformContainer subform = modal.subform(subformName);

        for (int i = 2; i < expectedSubformValues.getGherkinRows().size(); i++) {
            DataTableRow row = expectedSubformValues.getGherkinRows().get(i);

            String keyLabel = row.getCells().get(0);

            subform.selectKey(keyLabel);

            for (int j = 1; j < header.getCells().size(); j++) {
                String label = driver.getAliasTable().getAlias(header.getCells().get(j));
                String currentValue = modal.form().findFieldByLabel(label).getValue(ControlType.fromValue(type.getCells().get(j)));
                assertEquals(row.getCells().get(j), currentValue);
            }
        }
    }

    @And("^hide built-in columns$")
    public void hide_built_in_columns() throws Throwable {
        BsTable table = driver.tablePage().table();
        table.showAllColumns();
        table.hideBuiltInColumns();
    }

    @Then("^following table columns are visible$")
    public void following_table_columns_are_visible(List<String> expectedColumns) throws Throwable {
        BsTable table = driver.tablePage().table();
        List<String> columnNames = table.columnNames();

        assertEquals(columnNames.size(), expectedColumns.size());

        for (String expectedColumn : expectedColumns) {
            columnNames.remove(driver.getAliasTable().getAlias(expectedColumn));
        }
        assertTrue(columnNames.isEmpty());
    }
}
