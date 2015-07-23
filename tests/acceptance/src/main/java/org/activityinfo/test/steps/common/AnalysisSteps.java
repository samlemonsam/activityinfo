package org.activityinfo.test.steps.common;

import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.pageobject.web.Dashboard;
import org.activityinfo.test.pageobject.web.reports.DashboardPortlet;
import org.activityinfo.test.pageobject.web.reports.PivotTableEditor;

import javax.inject.Inject;
import java.util.List;

@ScenarioScoped
public class AnalysisSteps {

    @Inject
    private ApplicationDriver driver;
    
    @Inject
    private AliasTable aliasTable;

    @Then("^aggregating the indicators (.*) by (.*) should yield:$")
    public void aggregating_the_indicators_by_should_yield(String indicators, String dimensions, DataTable expected) throws Throwable {
        DataTable actual = driver.pivotTable(parseStringList(indicators), parseStringList(dimensions));
        expected.unorderedDiff(aliasTable.deAlias(actual));
    }
   
    @Then("^aggregating the indicator \"([^\"]*)\" by (.*) should yield:$")
    public void aggregating_the_indicator_by_should_yield(String indicatorName, String dimensions, DataTable expected) throws Throwable {
        DataTable actual = driver.pivotTable(indicatorName, parseStringList(dimensions));
        expected.diff(aliasTable.deAlias(actual));
    }


    @Then("^drill down on \"([^\"]*)\" should yield:$")
    public void drill_down_on_should_yield(String cellValue, DataTable expected) throws Throwable {
        DataTable actual = driver.drillDown(cellValue);
        expected.unorderedDiff(aliasTable.deAlias(actual));
    }

    @When("^I create a pivot table report aggregating the indicators (.*) by (.*)$")
    public void I_create_a_new_pivot_table_report(String indicatorName, String dimensions) throws Throwable {
        driver.pivotTable(indicatorName, parseStringList(dimensions));
    }

    @When("^I save the report as \"([^\"]*)\"$")
    public void I_save_the_report_as(String reportName) throws Throwable {
        UiApplicationDriver ui = (UiApplicationDriver) driver;
        PivotTableEditor editor = (PivotTableEditor) ui.getCurrentPage();
        editor.reportBar().rename(reportName);
        editor.reportBar().save();
    }

    @When("^I pin the report to my dashboard$")
    public void I_pin_the_report_to_my_dashboard() throws Throwable {
        UiApplicationDriver ui = (UiApplicationDriver) driver;
        PivotTableEditor editor = (PivotTableEditor) ui.getCurrentPage();
        editor.reportBar().pinToDashboard();
    }


    @Then("^the report \"([^\"]*)\" should be shown on my dashboard$")
    public void the_report_should_be_shown_on_my_dashboard(String reportName) throws Throwable {

        UiApplicationDriver ui = (UiApplicationDriver) driver;
        Dashboard dashboard = ui.getApplicationPage().navigateToDashboard();

        DashboardPortlet portlet = dashboard.findPortlet(reportName);
    }

    @Then("^the pivot table \"([^\"]*)\" should be shown on my dashboard with:$")
    public void the_pivot_table_should_be_shown_on_my_dashboard_with(String reportName, DataTable expectedData) throws Throwable {

        UiApplicationDriver ui = (UiApplicationDriver) driver;
        Dashboard dashboard = ui.getApplicationPage().navigateToDashboard();

        DashboardPortlet portlet = dashboard.findPortlet(reportName);
        DataTable actual = portlet.extractPivotTableData();
        expectedData.diff(aliasTable.deAlias(actual));
    }

    private List<String> parseStringList(String stringList) {
        String[] parts = stringList.split("and");
        List<String> strings = Lists.newArrayList();
        if(parts.length == 1) {
            strings.add(parts[0]);
        } else if(parts.length == 2) {
            String[] items = parts[0].split(",");
            for (String item : items) {
                strings.add(item.trim());
            }
            strings.add(parts[1].trim());
        }
        return strings;
    }

}
