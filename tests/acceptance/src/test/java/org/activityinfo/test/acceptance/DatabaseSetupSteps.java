package org.activityinfo.test.acceptance;

import com.google.common.base.Preconditions;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApiApplicationDriver;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.UserAccount;

import javax.inject.Inject;
import java.util.List;

import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;

@ScenarioScoped
public class DatabaseSetupSteps {
    
    @Inject
    private ApiApplicationDriver setupDriver;
    
    @Inject
    private ApplicationDriver driver;
    
    @Inject
    private Accounts accounts;
    
    private String currentDatabase;
    
    @After
    public final void cleanUp() throws Exception {
        driver.cleanup();
    }
    
    @Given("I have created a database \"(.*)\"")
    public void createDatabase(String databaseName) throws Exception {
        UserAccount account = accounts.any();
        driver.login(account);
        driver.setup().createDatabase(name(databaseName));
        this.currentDatabase = databaseName;
    }

    @Given("^I have created a form named \"(.*)\" in \"(.*)\"$")
    public void I_have_created_a_form_named_in(String formName, String databaseName) throws Throwable {
        driver.setup().createForm(name(formName), property("database", databaseName));
    }

    @Given("^I have created a form named \"([^\"]*)\"$")
    public void I_have_created_a_form_named(String formName) throws Throwable {
        I_have_created_a_form_named_in(formName, getCurrentDatabase());
    }

    @Given("^I have created a quantity field \"([^\"]*)\" in \"([^\"]*)\"$")
    public void I_have_created_a_quantity_field_in(String fieldName, String formName) throws Throwable {
        driver.setup().createField(
                property("form", formName),
                property("name", fieldName),
                property("type", "quantity"));
    }

    @Given("^I have submitted a \"([^\"]*)\" form with:$")
    public void I_have_submitted_a_form_with(String formName, List<FieldValue> values) throws Throwable {
        driver.setup().submitForm(formName, values);
    }


    @Given("^I have added partner \"([^\"]*)\" to \"([^\"]*)\"$")
    public void I_have_added_partner_to(String partnerName, String databaseName) throws Throwable {
        driver.setup().addPartner(partnerName, databaseName);
    }
    
    @Given("^I have added partner \"([^\"]*)\"$")
    public void I_have_added_partner_to(String partnerName) throws Throwable {
        I_have_added_partner_to(partnerName, getCurrentDatabase());
    }

    @Given("^I have created the project \"([^\"]*)\"$")
    public void I_have_created_the_project(String project) throws Throwable {
        driver.setup().createProject(
                property("name", project),
                property("database", getCurrentDatabase()));
    }

    private String getCurrentDatabase() {
        Preconditions.checkState(currentDatabase != null, "There has been no database mentioned yet");
        return currentDatabase;
    }

    @When("^I create a target named \"([^\"]*)\" for database \"([^\"]*)\"$")
    public void I_create_a_target_named_for_database_with(String targetName, String databaseName) throws Throwable {
        driver.createTarget(
                property("database", databaseName),
                property("name", targetName));

    }

    @When("^I create a target named \"([^\"]*)\" for partner (.*) in database (.*)$")
    public void I_create_a_target_for_partner(String targetName, String partnerName, String databaseName) throws Throwable {
        driver.createTarget(
                property("database", databaseName),
                property("partner", partnerName),
                property("name", targetName));
    }

    @When("^I create a target named \"([^\"]*)\" for partner \"([^\"]*)\" and project \"([^\"]*)\"$")
    public void I_create_a_target_named_for_partner_and_project(String target, String partner, String project) throws Throwable {
        driver.createTarget(property("database", getCurrentDatabase()),
                property("name", target),
                property("partner", partner),
                property("project", project));
    }

    @When("^I set the targets of \"(.*)\" to:$")
    public void I_set_the_targets_of_to(String targetName, List<FieldValue> values) throws Throwable {
        driver.setTargetValues(targetName, values);
    }
    
}
