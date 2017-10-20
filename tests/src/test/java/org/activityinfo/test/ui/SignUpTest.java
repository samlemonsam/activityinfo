package org.activityinfo.test.ui;

import com.google.common.base.Optional;
import org.activityinfo.test.TestRailCase;
import org.activityinfo.test.driver.mail.NotificationEmail;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.ConfirmPage;
import org.activityinfo.test.sut.UserAccount;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

/**
 * Tests that users sign up
 */

public class SignUpTest {
    @Rule
    public UiTestHarness harness = new UiTestHarness();

    /**
     *
     * Scenario: Successful SignUp
     * Given that I am a new user
     * When I SignUp for an account, I should receive an email confirmation
     * Then after following the link to confirm my account and choosing a password
     * I should be able to sign into my account
     * Then the dashboard should appear
     *
     */


    @Test
    @TestRailCase(1)
    public void successfulTest () throws IOException {
        UserAccount newUserAccount = harness.getEmailDriver().newAccount();
        harness.getSignUpPage().navigateTo().signUp(newUserAccount);
        Optional<NotificationEmail> email = harness.getEmailDriver().lastNotificationFor(newUserAccount);
        System.out.println(email.isPresent());
        if(!email.isPresent()){
            throw new AssertionError("No email for " + newUserAccount.getEmail());
        }

        URL confirmationURL = email.get().extractLink();
        System.out.println("Confirmation email " + confirmationURL);
        ConfirmPage confirmPage = harness.getConfirmPage(confirmationURL);
        ApplicationPage applicationPage = 
            confirmPage.confirmPasswordSubmitFormAndNavigateToApplicationPage("nosecret");


    }


}

