package org.activityinfo.test.ui;

import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.mail.EmailDriver;
import org.activityinfo.test.driver.mail.mailinator.MailinatorClient;
import org.activityinfo.test.pageobject.web.ConfirmPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.pageobject.web.SignUpPage;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.activityinfo.test.webdriver.ChromeWebDriverProvider;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.WebDriver;

import java.net.URL;

/**
 * Harness for tests written directly against the API.
 */
public class UiTestHarness extends ExternalResource {

    private final Server server;
    private final AliasTable aliasTable;
    private final DevServerAccounts accounts;
    private final WebDriver webDriver;
    private final EmailDriver emailDriver;

    public UiTestHarness() {
        server = new Server();
        System.out.println("Server: " + server.getRootUrl());

        aliasTable = new AliasTable();
        accounts = new DevServerAccounts(server);

        ChromeWebDriverProvider webDriverProvider = new ChromeWebDriverProvider();
        webDriver = webDriverProvider.start();

        emailDriver = new MailinatorClient();

    }

    public LoginPage getLoginPage() {
        return new LoginPage(webDriver, server);
    }

    public SignUpPage getSignUpPage() {
        return new SignUpPage(webDriver,server);
    }

    public ConfirmPage getConfirmPage(URL url){
        return new ConfirmPage(webDriver).navigateTo(url);
    }

    public UserAccount createAccount() {
        return accounts.any();
    }

    public String alias(String name){
        return aliasTable.getAlias(name);
    }

    public EmailDriver getEmailDriver() {
        return emailDriver;
    }


    @Override
    protected void after() {
        webDriver.close();
    }
}