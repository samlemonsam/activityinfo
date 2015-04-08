package org.activityinfo.test.steps.common;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.pageobject.web.entry.HistoryEntry;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import javax.inject.Inject;
import java.io.File;
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



    @Then("^the submission's history should show one change from (.*) to (.*)$")
    public void the_submission_s_history_should_show_one_change_from_to(String from, String to) throws Throwable {
        List<HistoryEntry> changes = driver.getSubmissionHistory();

        dumpChanges(changes);

        assertThat(changes, hasSize(2));
        assertThat(changes.get(0).getSummary(), Matchers.containsString("updated the entry"));
        assertThat(changes.get(0).getChanges(), hasSize(1));
        assertThat(changes.get(0).getChanges().get(0), CoreMatchers.containsString(from));
        assertThat(changes.get(0).getChanges().get(0), CoreMatchers.containsString(to));



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
    

}
