package org.activityinfo.test.steps.common;
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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import gherkin.formatter.model.DataTableRow;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.Sleep;
import org.activityinfo.test.driver.*;
import org.activityinfo.test.pageobject.bootstrap.BsFormPanel;
import org.activityinfo.test.pageobject.web.components.Form;
import org.activityinfo.test.pageobject.web.design.designer.*;
import org.openqa.selenium.support.ui.Select;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author yuriyz on 05/12/2015.
 */
@ScenarioScoped
public class DesignSteps {

    @Inject
    private ApplicationDriver driver;

    @When("^I have cloned a database \"([^\"]*)\" with name \"([^\"]*)\"$")
    public void I_have_cloned_a_database_with_name(String sourceDatabase, String targetDatabase) throws Throwable {
        driver.cloneDatabase(new TestObject(driver.getAliasTable(), new Property("sourceDatabase", sourceDatabase), new Property("targetDatabase", targetDatabase)));
    }

    @Then("^\"([^\"]*)\" database has \"([^\"]*)\" partner$")
    public void database_has_partner(String databaseName, String partnerName) throws Throwable {
        driver.assertVisible(ObjectType.PARTNER, true,
                new TestObject(driver.getAliasTable(), new Property("database", databaseName), new Property("name", partnerName)));
    }

    @Then("^\"([^\"]*)\" database has \"([^\"]*)\" form$")
    public void database_has_form(String databaseName, String formName) throws Throwable {
        driver.assertVisible(ObjectType.FORM, true,
                new Property("name", formName),
                new Property("database", databaseName)
        );
    }

    @Then("^\"([^\"]*)\" form has \"([^\"]*)\" form field with values in database \"([^\"]*)\":$")
    public void form_has_form_field_with_values(String formName, String formFieldName, String database, List<String> items) throws Throwable {
        driver.assertVisible(ObjectType.FORM_FIELD, true,
                new Property("name", formName),
                new Property("database", database),
                new Property("formFieldName", formFieldName),
                new Property("items", items));
    }

    @When("^I open the form designer for \"([^\"]*)\" in database \"([^\"]*)\"$")
    public void I_open_the_form_designer_for(String formName, String database) throws Throwable {
        driver.openFormDesigner(database, formName);
    }

    @When("^I open the table for \"([^\"]*)\" in database \"([^\"]*)\"$")
    public void I_open_the_table_for_in_database(String formName, String database) throws Throwable {
        driver.openFormTable(driver.getAliasTable().getAlias(database), driver.getAliasTable().getAlias(formName));
    }

    @Then("^form \"([^\"]*)\" in database \"([^\"]*)\" has \"([^\"]*)\" field represented by \"([^\"]*)\"$")
    public void form_in_database_has_field_represented_by(String formName, String databaseName, String fieldName, String controlType) throws Throwable {
        assertFieldControl(formName, databaseName, fieldName, controlType, Optional.<String>absent());
    }

    @Then("^form \"([^\"]*)\" in database \"([^\"]*)\" has \"([^\"]*)\" field represented by \"([^\"]*)\" with value \"([^\"]*)\"$")
    public void form_in_database_has_field_represented_by_with_value(String formName, String databaseName, String fieldName, String controlType, String selectedValue) throws Throwable {
        assertFieldControl(formName, databaseName, fieldName, controlType, Optional.fromNullable(selectedValue));
    }

    private void assertFieldControl(String formName, String databaseName, String fieldName, String controlType, Optional<String> selectedValue) {
        if (selectedValue.isPresent()) {
            selectedValue = Optional.of(driver.getAliasTable().getAlias(selectedValue.get()));
        }

        Form.FormItem formField = driver.getFormField(formName, databaseName, fieldName, selectedValue);

        switch (ControlType.fromValue(controlType)) {
            case SUGGEST_BOX:
                assertTrue(formField.isSuggestBox());
                break;
            case DROP_DOWN:
                assertTrue(formField.isDropDown());

                if (selectedValue.isPresent()) {
                    Select select = new Select(formField.getElement().find().select().first().element());
                    assertEquals(select.getFirstSelectedOption().getAttribute("text"), selectedValue.get());
                }
                break;
        }
    }

    @When("^I add a lock \"([^\"]*)\" on the database \"([^\"]*)\" from \"([^\"]*)\" to \"([^\"]*)\"$")
    public void I_add_a_lock_on_the_database_from_to(String lockName, String database, String startDate, String endDate) throws Throwable {
        driver.addLockOnDb(lockName, database, startDate, endDate, true);
    }

    @And("^I add a lock \"([^\"]*)\" on the form \"([^\"]*)\" from \"([^\"]*)\" to \"([^\"]*)\" in database \"([^\"]*)\"$")
    public void I_add_a_lock_on_the_form_from_to_in_database(String lockName, String formName, String startDate, String endDate, String database) throws Throwable {
        driver.addLockOnForm(lockName, database, formName, startDate, endDate, true);
    }

    @And("^I add a lock \"([^\"]*)\" on the project \"([^\"]*)\" from \"([^\"]*)\" to \"([^\"]*)\" in database \"([^\"]*)\"$")
    public void I_add_a_lock_on_the_project_from_to_in_database(String lockName, String projectName, String startDate, String endDate, String database) throws Throwable {
        driver.addLockOnProject(lockName, database, projectName, startDate, endDate, true);
    }

    @Then("^following fields should be visible in form designer:$")
    public void following_fields_should_be_visible_in_form_designer(List<String> fieldLabels) throws Throwable {
        for (String fieldLabel : fieldLabels) {
            driver.assertDesignerFieldVisible(fieldLabel);
        }
    }

    @Then("^following fields are not deletable in form designer:$")
    public void following_fields_are_not_deletable_in_form_designer(List<String> fieldLabels) throws Throwable {
        for (String fieldLabel : fieldLabels) {
            driver.assertDesignerFieldIsNotDeletable(fieldLabel);
        }
    }

    @Then("^reorder \"([^\"]*)\" designer field to position (\\d+)$")
    public void reorder_field_to_position(String fieldLabel, int positionOnPanel) throws Throwable {
        positionOnPanel--; // translate into machine number, position for human 2 means for machine 1
        driver.assertDesignerFieldReorder(fieldLabel, positionOnPanel);
    }

    @Then("^\"([^\"]*)\" designer field is mandatory$")
    public void designer_field_is_mandatory(String fieldLabel) throws Throwable {
        assertTrue("Designer field with label " + fieldLabel + " is not mandatory",
                driver.getDesignerField(fieldLabel).isMandatory());
    }

    @Then("^change designer field \"([^\"]*)\" with:$")
    public void change_designer_field_with(String fieldLabel, List<FieldValue> values) throws Throwable {
        driver.changeDesignerField(fieldLabel, values);
    }

    @Then("^\"([^\"]*)\" field properties are disabled in form designer for:$")
    public void field_properties_are_disabled_in_form_designer_for(String fieldProperties, List<String> fieldLabels) throws Throwable {
        for (String fieldLabel : fieldLabels) {
            for (DesignerFieldPropertyType fieldPropertyType : DesignerFieldPropertyType.fromCommaSeparateString(fieldProperties)) {
                driver.assertDesignerFieldHasProperty(fieldLabel, fieldPropertyType, false);
            }
        }
    }

    @And("^I add \"([^\"]*)\" to database \"([^\"]*)\" with partner \"([^\"]*)\" and permissions$")
    public void I_add_to_database_with_partner_and_permissions(String userEmail, String databaseName, String partner, List<FieldValue> permissions) throws Throwable {
        driver.addUserToDatabase(userEmail, databaseName, partner, permissions);
    }

    @And("^drop field:$")
    public void drop_field(DataTable dataTable) throws Throwable {
        FormDesignerPage page = (FormDesignerPage) driver.getCurrentPage();

        Map<String, String> fieldProperties = TableDataParser.asMap(dataTable);

        for (Map.Entry<String, String> entry : fieldProperties.entrySet()) {
            page.fields().dropNewField(entry.getValue());
            page.selectFieldByLabel(entry.getValue());

            String label = driver.getAliasTable().createAlias(entry.getKey());
            page.fieldProperties().setLabel(label);
        }

        page.save();

    }

    @And("^set relevance for \"([^\"]*)\" field:$")
    public void set_relevance_for_field(String fieldName, DataTable dataTable) throws Throwable {
        FormDesignerPage page = (FormDesignerPage) driver.getCurrentPage();
        page.setRelevance(driver.getAliasTable().getAlias(fieldName), dataTable, driver.getAliasTable());
        page.save();
    }

    // accepts table :
    //| label        | type        | container  |
    //| MySection    | Section     | root       |
    //| MyText       | Text        | MySection  |
    @And("^drop field in:$")
    public void drop_field_in(DataTable dataTable) throws Throwable {
        FormDesignerPage page = (FormDesignerPage) driver.getCurrentPage();

        for (int i = 1; i < dataTable.getGherkinRows().size(); i++) {
            DataTableRow row = dataTable.getGherkinRows().get(i);

            List<String> cells = row.getCells();

            String label = cells.get(0);
            String fieldType = cells.get(1);
            String containerLabel = cells.get(2);

            DropLabel dropLabel = page.fields().dropLabel(fieldType);
            DropPanel dropPanel = page.dropPanel(containerLabel);

            dropPanel.dragAndDrop(dropLabel);
            Sleep.sleepMillis(100);

            if ("root".equalsIgnoreCase(containerLabel) && (
                    fieldType.equalsIgnoreCase("Section") || fieldType.equalsIgnoreCase("Sub Form"))) {
                DropPanel containerDropPanel = page.dropPanel(fieldType);
                containerDropPanel.getContainer().clickWhenReady();

                page.containerProperties().setLabel(label);

            } else {
                page.selectFieldByLabel(fieldType);
                page.fieldProperties().setLabel(driver.getAliasTable().createAlias(label));
            }
        }

        page.save();
    }

    @And("^set \"([^\"]*)\" subform to \"([^\"]*)\"$")
    public void set_subform_to(String subformName, String subformType) throws Throwable {
        FormDesignerPage page = (FormDesignerPage) driver.getCurrentPage();

        DropPanel dropPanel = page.dropPanel(subformName);

        dropPanel.getContainer().clickWhenReady();
        page.containerProperties().selectProperty(I18N.CONSTANTS.type(), subformType);
        page.save();
    }

    @And("^choose reference for field \"([^\"]*)\"$")
    public void choose_reference_for_field(String fieldLabel, DataTable dataTable) throws Throwable {
        FormDesignerPage page = (FormDesignerPage) driver.getCurrentPage();

        page.selectFieldByLabel(driver.alias(fieldLabel));

        PropertiesPanel fieldProperties = page.fieldProperties();

        for (int i = 0; i < dataTable.getGherkinRows().size(); i++) {
            DataTableRow row = dataTable.getGherkinRows().get(i);

            fieldProperties.chooseFormDialog().set(row.getCells(), driver.getAliasTable());
        }

        page.save();
    }

    @Then("^\"([^\"]*)\" field has instances:$")
    public void field_has_instances(String fieldLabel, List<String> expectedItems) throws Throwable {
        Preconditions.checkNotNull(driver.getCurrentModal());

        BsFormPanel.BsField field = driver.getCurrentModal().form().findFieldByLabel(fieldLabel);

        List<String> presentItems = Lists.newArrayList(field.availableItems());

        assertTrue(presentItems.containsAll(expectedItems));
    }
}
