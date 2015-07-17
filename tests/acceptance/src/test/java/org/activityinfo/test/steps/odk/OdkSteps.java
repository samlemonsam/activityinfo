package org.activityinfo.test.steps.odk;

import com.google.inject.Inject;
import cucumber.api.java.After;
import cucumber.api.java.en.Then;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.OdkApplicationDriver;

@ScenarioScoped
public class OdkSteps {
    
    
    @Inject
    private OdkApplicationDriver odkDriver;


    @Then("^\"([^\"]*)\" should not appear in the list of blank forms in ODK$")
    public void should_not_appear_in_the_list_of_blank_forms_in_ODK(String formName) throws Throwable {
        odkDriver.assertFormIsNotPresent(formName);
    }
    
    @After
    public void shutdown() {
        odkDriver.quit();
    }
}
