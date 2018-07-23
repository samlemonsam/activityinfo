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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.mail.EmailDriver;
import org.activityinfo.test.driver.mail.NotificationEmail;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.ConfirmPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.pageobject.web.SignUpPage;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;

import javax.inject.Inject;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

@ScenarioScoped
public class SignUpSteps {

    @Inject
    private SignUpPage signUpPage;

    @Inject
    private LoginPage loginPage;

    @Inject
    private ConfirmPage confirmPage;

    @Inject
    private EmailDriver emailDriver;

    @Inject
    private Server server;

    private UserAccount newUserAccount;

    private long signUpTime;

    private URL confirmationUrl;

    private ApplicationPage applicationPage;


    @When("^I sign up for a new user account$")
    public void I_sign_up_for_a_new_user_account() throws Throwable {
        
        newUserAccount = emailDriver.newAccount();

        signUpTime = System.currentTimeMillis();
        signUpPage.navigateTo().signUp(newUserAccount);
    }



    @Then("^I should receive an email with a link to confirm my address$")
    public void I_should_receive_an_email_with_a_link_to_confirm_my_address() throws Throwable {

        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<NotificationEmail> email;
        do {
            email = emailDriver.lastNotificationFor(newUserAccount);
            if(email.isPresent()) {
                break;
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        } while (stopwatch.elapsed(TimeUnit.SECONDS) < 90);

        if(!email.isPresent()) {
            throw new AssertionError("No email for " + newUserAccount.getEmail());
        }

        confirmationUrl = email.get().extractLink();

        System.out.println("Confirmation URL: " + confirmationUrl);

        assertThat(confirmationUrl, hasProperty("host", equalTo(server.getRootUri().getHost())));
        assertThat(confirmationUrl, hasProperty("port", equalTo(server.getRootUri().getPort())));
    }

    @When("^I follow the link$")
    public void I_follow_the_link() throws Throwable {
        confirmPage.navigateTo(confirmationUrl);
    }

    @Given("^I have signed up for a new account$")
    public void I_have_signed_up_for_a_new_account() throws Throwable {
        I_sign_up_for_a_new_user_account();
    }

    @Given("^I have confirmed my account$")
    public void I_have_confirmed_my_account() throws Throwable {
        I_should_receive_an_email_with_a_link_to_confirm_my_address();
        I_follow_the_link();
        I_choose_a_password();
    }

    @When("^I follow the invitation link again$")
    public void I_follow_the_invitation_link_again() throws Throwable {
        Preconditions.checkState(confirmationUrl != null, "Confirmation link not seen yet");

        confirmPage.navigateTo(confirmationUrl);
    }

    @When("^I choose a password$")
    public void I_choose_a_password() throws Throwable {
        confirmPage.confirmPasswordSubmitFormAndNavigateToApplicationPage(newUserAccount.getPassword());
    }

    @When("^I try to choose an empty password$")
    public void I_try_to_choose_an_empty_password() throws Throwable {
        applicationPage = confirmPage.confirmPasswordSubmitFormAndNavigateToApplicationPage("");
    }


    @And("^I choose the password \"([^\"]*)\"$")
    public void I_try_to_choose_the_password(String password) throws Throwable {
        applicationPage = confirmPage.confirmPasswordSubmitFormAndNavigateToApplicationPage(password);
        newUserAccount = new UserAccount(newUserAccount.getEmail(), password);
    }

    @Then("^I should receive a message that the link has already been used$")
    public void I_should_receive_a_message_that_the_link_has_already_been_used() throws Throwable {
        confirmPage.assertLinkIsInvalid();
    }

    @Then("^I should receive a message that my password is too short$")
    public void I_should_receive_a_message_that_my_password_is_too_short() throws Throwable {
        confirmPage.assertPasswordIsTooShort();
    }

    @And("^I login with my new password$")
    public void I_login_with_my_new_password() throws Throwable {
        loginPage.navigateTo().loginAs(newUserAccount).andExpectSuccess();
    }

    @And("^I login with the password \"([^\"]*)\"$")
    public void I_login_with_the_password(String arg1) throws Throwable {
        loginPage.navigateTo().loginAs(newUserAccount).andExpectSuccess();
    }
}
