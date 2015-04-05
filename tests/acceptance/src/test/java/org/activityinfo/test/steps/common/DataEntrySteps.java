package org.activityinfo.test.steps.common;

import com.google.inject.Singleton;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.FieldValue;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@Singleton
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
}
