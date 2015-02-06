package org.activityinfo.test.acceptance;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.FieldValue;

import javax.inject.Inject;
import java.util.List;

import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;

@ScenarioScoped
public class DatabaseSetupSteps {
    
    @Inject
    private ApplicationDriver driver;
    
    @Given("I have created a database \"(.*)\"")
    public void createDatabase(String databaseName) throws Exception {
        driver.login();
        driver.createDatabase(name(databaseName));
    }

    @And("^I have created a form named \"(.*)\" in \"(.*)\"$")
    public void I_have_created_a_form_named_in(String formName, String databaseName) throws Throwable {
        driver.createForm(name(formName), property("database", databaseName));
    }

    @And("^I have created a quantity field \"([^\"]*)\" in \"([^\"]*)\"$")
    public void I_have_created_a_quantity_field_in(String fieldName, String formName) throws Throwable {
        driver.createField(
                property("form", formName), 
                property("name", fieldName), 
                property("type", "quantity"));
    }

    @And("^I have submitted a \"([^\"]*)\" form with:$")
    public void I_have_submitted_a_form_with(String formName, List<FieldValue> values) throws Throwable {
        driver.submitForm(formName, values);
    }


    @And("^I have added partner \"([^\"]*)\" to \"([^\"]*)\"$")
    public void I_have_added_partner_to(String partnerName, String databaseName) throws Throwable {
        driver.addPartner(partnerName, databaseName);
    }

    @When("^I create a target named \"([^\"]*)\" for database \"([^\"]*)\"$")
    public void I_create_a_target_named_for_database_with(String targetName, String databaseName) throws Throwable {
        driver.createTarget(property("database", databaseName), property("name", targetName));
    }
}
