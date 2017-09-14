package org.activityinfo.test.ui;

import com.google.appengine.repackaged.com.google.common.base.Verify;
import com.google.common.base.Optional;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.test.TestRailCase;
import org.activityinfo.test.capacity.model.User;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.driver.mail.EmailDriver;
import org.activityinfo.test.driver.mail.NotificationEmail;
import org.activityinfo.test.driver.mail.mailinator.MailinatorClient;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.ConfirmPage;
import org.activityinfo.test.pageobject.web.SignUpPage;
import org.activityinfo.test.steps.web.SignUpSteps;
import org.activityinfo.test.sut.UserAccount;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.xmlbeans.impl.store.QueryDelegate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.SendKeysAction;

import javax.security.auth.callback.ConfirmationCallback;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.security.SecureRandom;
import java.util.TreeSet;

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
        harness.getEmailDriver().lastNotificationFor(newUserAccount).get().extractLink();

     }}

