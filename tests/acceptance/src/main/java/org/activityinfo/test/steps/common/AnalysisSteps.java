package org.activityinfo.test.steps.common;

import com.google.common.base.Preconditions;
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
import org.activityinfo.test.pageobject.web.reports.ReportsTab;
import org.activityinfo.test.sut.Accounts;
import org.hamcrest.Matchers;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;

@ScenarioScoped
public class AnalysisSteps {

    @Inject
    private ApplicationDriver driver;
    
    @Inject
    private AliasTable aliasTable;
    
    @Inject
    private Accounts accounts;
    
    private String currentReport;

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
        driver.pivotTable(parseStringList(indicatorName), parseStringList(dimensions));
    }

    @When("^I save the report as \"([^\"]*)\"$")
    public void I_save_the_report_as(String reportName) throws Throwable {
        UiApplicationDriver ui = (UiApplicationDriver) driver;
        PivotTableEditor editor = (PivotTableEditor) ui.getCurrentPage();
        editor.reportBar().rename(aliasTable.createAlias(reportName));
        editor.reportBar().save();
        
        currentReport = reportName;
    }

    @When("^I pin the report to my dashboard$")
    public void I_pin_the_report_to_my_dashboard() throws Throwable {
        UiApplicationDriver ui = (UiApplicationDriver) driver;
        PivotTableEditor editor = (PivotTableEditor) ui.getCurrentPage();
        editor.reportBar().pinToDashboard();
    }

    @When("^I share the report with users of the \"([^\"]*)\" database$")
    public void I_share_the_report_with_users_of_the_database(String databaseName) throws Throwable {
        UiApplicationDriver ui = (UiApplicationDriver) driver;
        PivotTableEditor editor = (PivotTableEditor) ui.getCurrentPage();
        
        editor.reportBar().share().shareWith(aliasTable.getAlias(databaseName), true).ok();
    }

    @When("^I share the report with users of the \"([^\"]*)\" database as a default dashboard report$")
    public void I_share_the_report_with_users_of_the_database_as_a_default_dashboard_report(String databaseName) throws Throwable {
        UiApplicationDriver ui = (UiApplicationDriver) driver;
        PivotTableEditor editor = (PivotTableEditor) ui.getCurrentPage();

        editor.reportBar().share().putOnDashboard(aliasTable.getAlias(databaseName)).ok();
    }
    
    @Then("^the report \"([^\"]*)\" should be shown on my dashboard$")
    public void the_report_should_be_shown_on_my_dashboard(String reportName) throws Throwable {
        assertThat(driver.getDashboardPortlets(), contains(reportName));
    }


    @Then("^the report should be shown on my dashboard with:$")
    public void the_report_should_be_shown_on_my_dashboard_with(DataTable expected) throws Throwable {
        the_pivot_table_should_be_shown_on_my_dashboard_with(getCurrentReport(), expected);
    }

    @Then("^the pivot table \"([^\"]*)\" should be shown on my dashboard with:$")
    public void the_pivot_table_should_be_shown_on_my_dashboard_with(String reportName, DataTable expectedData) throws Throwable {

        UiApplicationDriver ui = (UiApplicationDriver) driver;
        Dashboard dashboard = ui.getApplicationPage().navigateToDashboard();

        DashboardPortlet portlet = dashboard.findPortlet(aliasTable.getAlias(reportName));
        DataTable actual = portlet.extractPivotTableData();
        expectedData.diff(aliasTable.deAlias(actual));
    }


    @Then("^the report should not be shown on the dashboard of \"([^\"]*)\"$")
    public void the_report_should_not_be_shown_on_the_dashboard_of(String userName) throws Throwable {
        driver.login(accounts.ensureAccountExists(userName));
        
        assertThat(driver.getDashboardPortlets(), not(contains(getCurrentReport())));
    }

    @Then("^the report should be shown on (.+)'s dashboard$")
    public void the_report_should_not_be_shown_on_users_dashboard(String userName) throws Throwable {
        driver.login(accounts.ensureAccountExists(userName));
        assertThat(driver.getDashboardPortlets(), contains(getCurrentReport()));
    }

    private String getCurrentReport() {
        Preconditions.checkState(currentReport != null, "No current report");

        return currentReport;
    }

    @Then("^\"([^\"]*)\" should appear in my list of saved reports$")
    public void should_appear_in_my_list_of_saved_reports(String reportName) throws Throwable {
        assertThat(driver.getSavedReports(), contains(reportName));
    }
    
    
    @Then("^the report should not appear in (.+)'s list of saved reports$")
    public void the_report_should_not_appear_in_users_list_of_saved_reports(String userName) throws Throwable {

        driver.login(accounts.ensureAccountExists(userName));

        assertThat(driver.getSavedReports(), not(contains(getCurrentReport())));
    }

    @Then("^the report should appear in (.+)'s list of saved reports$")
    public void the_report_should_appear_in_users_list_of_saved_reports(String userName) throws Throwable {

        driver.login(accounts.ensureAccountExists(userName));

        assertThat(driver.getSavedReports(), contains(getCurrentReport()));
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
