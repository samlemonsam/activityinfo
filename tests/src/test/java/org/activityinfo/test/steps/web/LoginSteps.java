/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.test.steps.web;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;

import javax.inject.Inject;


@ScenarioScoped
public class LoginSteps {

    @Inject
    private Server server;

    @Inject
    private ApplicationPage applicationPage;

    @Inject
    private Accounts accounts;

    @Inject
    private LoginPage loginPage;

    @Inject
    private ApplicationDriver applicationDriver;
    

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

        logoutIfloggedIn();
        loginPage.navigateTo().loginAs(account);
    }


    @When("^I login as \"([^\"]*)\" with password \"([^\"]*)\"$")
    public void I_login_as_with_password(String email, String password) throws Throwable {

        loginPage
            .navigateTo()
            .loginAs(new UserAccount(email, password));
    }

    @Then("^my dashboard should open$")
    public void my_dashboard_should_open() throws Throwable {
        applicationPage.dashboard().assertAtLeastOnePortletIsVisible();
    }

    @Then("^I should see an error alert$")
    public void I_should_see_an_error_alert() throws Throwable {
        loginPage.assertErrorMessageIsVisible();
    }

    @Given("^that I am logged in as \"([^\"]*)\"$")
    public void that_I_am_logged_in_as(String email) throws Throwable {
        UserAccount account = accounts.ensureAccountExists(email);
        loginPage
            .navigateTo()
            .loginAs(account)
            .andExpectSuccess();
    }

    @Then("^I should receive a message that my browser is not unsupported$")
    public void I_should_receive_a_message_that_my_browser_is_unsupported() throws Throwable {
        loginPage.assertBrowserUnsupportedPageIsVisible();
    }

    @When("^I logout$")
    public void I_logout() throws Throwable {
        applicationPage.openSettingsMenu().logout();
    }

    public void logoutIfloggedIn() {
        try {
            applicationPage.openSettingsMenu().logout();
        } catch (Exception e) {
            // ignore
        }
    }
}
