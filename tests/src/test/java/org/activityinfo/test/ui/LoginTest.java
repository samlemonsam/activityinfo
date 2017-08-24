package org.activityinfo.test.ui;

import org.activityinfo.test.api.ApiTestHarness;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.sut.UserAccount;
import org.junit.Test;

/**
 * Tests that users can log in
 *
 */
public class LoginTest {


    private UiTestHarness harness = new UiTestHarness();

    /**
     *
     * Scenario: Successful login
     * Given that the user "auto.qa@bedatadriven.com" is signed up
     * When I login as "auto.qa@bedatadriven.com" with my correct password
     * Then my dashboard should open
     */
    @Test
    public void successful() {


    }


}
