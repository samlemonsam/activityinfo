package org.activityinfo.test.acceptance;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.Dashboard;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.pageobject.api.PageBinder;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.SystemUnderTest;
import org.activityinfo.test.sut.UserAccount;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class LoginSteps {

    @Inject
    private SystemUnderTest server;

    @Inject
    private PageBinder binder;

    @Inject
    private Accounts accounts;

    private LoginPage loginPage;


    @Given("^that the user \"([^\"]*)\" is not signed up$")
    public void that_the_user_is_not_signed_up(String email) throws Throwable {
        // NOOP for now
    }

    @Given("^that the user \"([^\"]*)\" is signed up$")
    public void that_the_user_is_signed_up(String email) throws Throwable {
        accounts.ensureAccountExists(email);
    }

    @When("^I login as \"([^\"]*)\" with my correct password$")
    public void I_login_as_with_my_password(String email) throws Throwable {
        UserAccount account = accounts.ensureAccountExists(email);

        loginPage = binder.navigateTo(LoginPage.class);
        loginPage.loginAs(account);
    }

    @When("^I login as \"([^\"]*)\" with password \"([^\"]*)\"$")
    public void I_login_as_with_password(String email, String password) throws Throwable {

        loginPage = binder.navigateTo(LoginPage.class);
        loginPage.loginAs(new UserAccount(email, password));
    }

    @Then("^my dashboard should open$")
    public void my_dashboard_should_open() throws Throwable {
        ApplicationPage app = binder.assertIsOpen(ApplicationPage.class);
        Dashboard dashboard = app.assertCurrentPageIs(Dashboard.class);
        dashboard.assertThatAtLeastOnePortletIsVisible();
    }

    @Then("^I should see an error alert$")
    public void I_should_see_an_error_alert() throws Throwable {
        loginPage.assertErrorMessageIsVisible();
    }
}
