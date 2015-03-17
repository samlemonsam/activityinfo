package org.activityinfo.test.capacity.scenario;

import org.activityinfo.test.capacity.agent.Agent;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.ApiApplicationDriver;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.activityinfo.test.webdriver.PhantomJsProvider;
import org.activityinfo.test.webdriver.WebDriverProvider;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an independent test scenario which might involve multiple simulated users
 * 
 */
public class Scenario {

    private final WebDriverProvider webDriverProvider;
    private Server server;
    private DevServerAccounts accounts;
    private AliasTable aliasTable;
    
    private Map<String, Integer> nextNumber = new HashMap<>();

    public Scenario(Server server, DevServerAccounts accounts) {
        this.server = server;
        this.accounts = accounts;
        this.aliasTable = new AliasTable();
        this.webDriverProvider = new PhantomJsProvider();
    }
    
    public Agent newAgent(String name) {
        int number = 1;
        if(nextNumber.containsKey(name)) {
            number = nextNumber.get(name);
        }
        nextNumber.put(name, number+1);
        
        String testHandle = name + " #" + number;
        
        ApiApplicationDriver driver = new ApiApplicationDriver(server, accounts, aliasTable);
        UserAccount account = accounts.createAccount(testHandle);
        driver.login(account);
        return new Agent(this, testHandle, account, driver);
    }

    public Server getServer() {
        return server;
    }

    public AliasTable getAliasTable() {
        return aliasTable;
    }
    
    public WebDriver startWebDriver() {
        return webDriverProvider.start("Capacity Test", null);
    }

    public DevServerAccounts getAccounts() {
        return accounts;
    }
}
