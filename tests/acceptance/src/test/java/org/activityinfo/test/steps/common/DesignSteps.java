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

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.ObjectType;
import org.activityinfo.test.driver.Property;
import org.activityinfo.test.driver.TestObject;

import javax.inject.Inject;
import java.util.List;

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
        driver.openFormTable(database, formName);
    }

    @Then("^form \"([^\"]*)\" in database \"([^\"]*)\" has \"([^\"]*)\" field represented by \"([^\"]*)\"$")
    public void form_in_database_has_field_represented_by(String formName, String databaseName, String fieldName, String controlType) throws Throwable {
        driver.assertFieldVisible(formName, databaseName, fieldName, controlType);
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
}
