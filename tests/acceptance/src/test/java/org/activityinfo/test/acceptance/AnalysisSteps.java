package org.activityinfo.test.acceptance;

import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.TableData;

import javax.inject.Inject;
import java.util.List;

@ScenarioScoped
public class AnalysisSteps {

    @Inject
    private ApplicationDriver driver;
    
    @Inject
    private AliasTable aliasTable;
    
   
    @Then("^aggregating the indicator \"([^\"]*)\" by (.*) should yield:$")
    public void aggregating_the_indicator_by_should_yield(String indicatorName, String dimensions, DataTable expected) throws Throwable {
        DataTable actual = driver.pivotTable(indicatorName, parseDimensions(dimensions));
        expected.diff(aliasTable.alias(actual));
    }

    private List<String> parseDimensions(String dimensionList) {
        String[] parts = dimensionList.split("and");
        List<String> dimensions = Lists.newArrayList();
        if(parts.length == 1) {
            dimensions.add(parts[0]);
        } else if(parts.length == 2) {
            String[] items = parts[0].split(",");
            for (String item : items) {
                dimensions.add(item.trim());
            }
            dimensions.add(parts[1].trim());
        }
        return dimensions;
    }
}
