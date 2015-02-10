package org.activityinfo.test.acceptance;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;

import javax.inject.Inject;

@ScenarioScoped
public class AnalysisSteps {

    @Inject
    private ApplicationDriver driver;
    
   
    @Then("^aggregating the indicator \"([^\"]*)\" by (.*) should yield:$")
    public void aggregating_the_indicator_by_should_yield(String indicatorName, String dimensions, DataTable dataTable) throws Throwable {
        driver.pivotTable(indicatorName, dimensions);
        
    }
}
