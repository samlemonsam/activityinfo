package org.activityinfo.test.ui;

import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.ApiApplicationDriver;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.activityinfo.test.webdriver.ChromeWebDriverProvider;
import org.openqa.selenium.WebDriver;

/**
 * Harness for tests written directly against the API.
 */
public class UiTestHarness {

    private final Server server;
    private final AliasTable aliasTable;
    private final DevServerAccounts accounts;
    private final UiApplicationDriver driver;

    public UiTestHarness() {
        server = new Server();
        System.out.println("Server: " + server.getRootUrl());

        aliasTable = new AliasTable();
        accounts = new DevServerAccounts(server);

        ChromeWebDriverProvider webDriverProvider = new ChromeWebDriverProvider();
        WebDriver webDriver = webDriverProvider.start();

        ApiApplicationDriver apiDriver = new ApiApplicationDriver(server, accounts, aliasTable);
        driver = new UiApplicationDriver(
            apiDriver, new LoginPage(webDriver, server),
            aliasTable, accounts);
    }

    public LoginPage getLoginPage() {
        return driver.getLoginPage();
    }

    public UserAccount createAccount() {
        return accounts.any();
    }
}