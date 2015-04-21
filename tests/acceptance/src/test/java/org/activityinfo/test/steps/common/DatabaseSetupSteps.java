package org.activityinfo.test.steps.common;

import com.google.common.base.Preconditions;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.driver.ObjectType;
import org.activityinfo.test.driver.Property;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.UserAccount;

import javax.inject.Inject;
import java.util.List;

import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;

@ScenarioScoped
public class DatabaseSetupSteps {

    @Inject
    private ApplicationDriver driver;
    
    @Inject
    private Accounts accounts;
    
    private String currentDatabase;
    private String currentForm;
    
    private int targetIndex = 1;

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
        
        this.currentForm = formName;
    }

    @Given("^I have created a monthly form named \"([^\"]*)\"$")
    public void I_have_created_a_monthly_form_named(String formName) throws Throwable {
        driver.setup().createForm(name(formName), 
                property("database", currentDatabase),
                property("reportingFrequency", "monthly"));

    }

    @Given("^I have created a form named \"([^\"]*)\" with location type \"([^\"]*)\"$")
    public void I_have_created_a_form_named_with_location_type(String name, String locationType) throws Throwable {
        driver.setup().createForm(
                name(name),
                property("locationType", locationType),
                property("database", getCurrentDatabase()));
    }

    @Given("^I have created a form named \"([^\"]*)\"$")
    public void I_have_created_a_form_named(String formName) throws Throwable {
        I_have_created_a_form_named_in(formName, getCurrentDatabase());
    }

    @Given("^I have created a (text|quantity) field \"([^\"]*)\" in \"([^\"]*)\"$")
    public void I_have_created_a_field_in(String fieldType, String fieldName, String formName) throws Throwable {
        driver.setup().createField(
                property("form", formName),
                property("name", fieldName),
                property("type", fieldType));
    }

    @And("^I have created a quantity field \"([^\"]*)\" in \"([^\"]*)\" with code \"([^\"]*)\"$")
    public void I_have_created_a_quantity_field_in_with_code(String fieldName, String formName, String fieldCode) throws Throwable {
        driver.setup().createField(
                property("form", formName),
                property("name", fieldName),
                property("type", "quantity"),
                property("code", fieldCode)
        );

        this.currentForm = formName;
    }

    @Given("^I have created a calculated field \"([^\"]*)\" in \"([^\"]*)\" with expression \"([^\"]*)\"$")
    public void I_have_created_a_calculated_field_in(String fieldName, String formName, String expression) throws Throwable {
        driver.setup().createField(
                property("form", formName),
                property("name", fieldName),
                property("type", "quantity"),
                property("expression", expression),
                property("calculatedAutomatically", true));
    }


    @Given("^I have created a (text|quantity) field \"([^\"]*)\"$")
    public void I_have_created_a_field_in(String fieldType, String fieldName) throws Throwable {
        Preconditions.checkState(currentForm != null, "Create a form first");
        
        I_have_created_a_field_in(fieldType, fieldName, currentForm);
    }

    @Given("^I have created a enumerated field \"([^\"]*)\" with items:$")
    public void I_have_created_a_enumerated_field_with_options(String fieldName, List<String> items) throws Throwable {
        Preconditions.checkState(currentForm != null, "No current form");
        
        driver.setup().createField(
                property("form", currentForm),
                property("name", fieldName),
                property("type", "enumerated"),
                property("items", items));
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

    @Given("^I have created a target named \"([^\"]*)\" for database \"([^\"]*)\"$")
    public void I_have_created_a_target_named_for_database(String targetName, String database) throws Throwable {
        driver.setup().createTarget(
                property("database", database),
                property("name", targetName));
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

    @When("^I create a target named \"([^\"]*)\"$")
    public void I_create_a_target(String targetName) throws Throwable {
        driver.createTarget(
                property("database", getCurrentDatabase()),
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


    @When("^I create a target for project \"([^\"]*)\" with values:$")
    public void I_create_a_target_for_project_with_values(String project, List<FieldValue> values) throws Throwable {
        String targetName = nextTargetName();

        driver.createTarget(
                property("name", targetName),
                property("database", getCurrentDatabase()),
                property("project", project));
        driver.setTargetValues(targetName, values);
    
    }

    @When("^I set the targets of \"(.*)\" to:$")
    public void I_set_the_targets_of_to(String targetName, List<FieldValue> values) throws Throwable {
        driver.setTargetValues(targetName, values);
    }

    @When("^I create a target with values:$")
    public void I_create_a_target(List<FieldValue> targetValues) throws Throwable {
        String targetName = nextTargetName();
        driver.createTarget(
                property("database", getCurrentDatabase()),
                property("name", targetName));
        driver.setTargetValues(targetName, targetValues);
    }

    private String nextTargetName() {
        return "target" + (targetIndex++);
    }

    @When("^I create a target for partner (.*) with values:$")
    public void I_create_for_partner(String partnerName, List<FieldValue> targetValues) throws Throwable {
        String targetName = nextTargetName();
        driver.createTarget(
                property("database", getCurrentDatabase()),
                property("partner", partnerName),
                property("name", targetName));
        driver.setTargetValues(targetName, targetValues);
    }

    @Given("^I have granted ([^ ]+) permission to (.+) on behalf of \"(.+)\"$")
    public void I_have_granted_permission(String accountEmail, String permission, String partner) throws Throwable {
        driver.setup().grantPermission(
                property("database", getCurrentDatabase()),
                property("user", accountEmail),
                property("permissions", permission),
                property("partner", partner));    
    }

    @Given("^I have created a location type \"([^\"]*)\"$")
    public void I_have_created_a_location_type(String name) throws Throwable {
        driver.setup().createLocationType(
                property("database", getCurrentDatabase()),
                property("name", name));
    }

    @Given("^I have created a location \"([^\"]*)\" in \"([^\"]*)\" with code \"([^\"]*)\"$")
    public void I_have_created_a_location_in_with_code(String name, String locationType, String code) throws Throwable {
        driver.setup().createLocation(
                property("name", name),
                property("locationType", locationType),
                property("code", code));
    }

    @Then("^Location type \"(.*?)\" should be visible\\.$")
    public void location_type_should_appear_in_tree(String locationTypeName) throws Throwable {
        driver.assertVisible(ObjectType.LOCATION_TYPE, true,
                new Property("name", locationTypeName),
                new Property("database", getCurrentDatabase())
        );
    }

    @When("^I have removed the location type \"(.*?)\"$")
    public void i_have_removed_the_location_type_in(String locationTypeName) throws Throwable {
        driver.delete(ObjectType.LOCATION_TYPE,
                new Property("name", locationTypeName),
                new Property("database", getCurrentDatabase())
        );
    }

    @Then("^Location type \"(.*?)\" is no longer visible\\.$")
    public void location_type_should_disappear_from_tree(String locationTypeName) throws Throwable {
        driver.assertVisible(ObjectType.LOCATION_TYPE, false,
                new Property("name", locationTypeName),
                new Property("database", getCurrentDatabase())
        );
    }

    @Given("^I have imported (\\d+) locations into \"([^\"]*)\"$")
    public void I_have_imported_locations_into(int locationCount, String locationType) throws Throwable {
        for(int i=0;i<locationCount;++i) {
            driver.setup().createLocation(
                    property("locationType", locationType),
                    property("name", "Location " + i),
                    property("code", "LOC" + i));
        }
    }

    @And("^I open a new session as (.+)$")
    public void I_open_a_new_session_as_(String user) throws Throwable {
        driver.login(accounts.ensureAccountExists(user));
    }
}
