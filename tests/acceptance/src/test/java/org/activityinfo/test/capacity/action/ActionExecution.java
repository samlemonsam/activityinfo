package org.activityinfo.test.capacity.action;

import org.activityinfo.test.capacity.CapacityTest;
import org.activityinfo.test.capacity.model.ScenarioContext;
import org.activityinfo.test.capacity.model.UserRole;
import org.activityinfo.test.config.ConfigurationError;
import org.activityinfo.test.driver.ApiApplicationDriver;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Runs a {@code UserAction} with the ApiApplicationDriver
*/
public class ActionExecution implements Runnable {
    
    private static final Logger LOGGER = Logger.getLogger(ActionExecution.class.getName());
    
    private final ScenarioContext context;
    private final UserRole user;
    private final UserAction action;

    public ActionExecution(ScenarioContext context, UserRole user, UserAction action) {
        this.context = context;
        this.user = user;
        this.action = action;
    }
    
    @Override
    public void run() {
        CapacityTest.CONCURRENT_USERS.inc();
        try {

            ApiApplicationDriver driver = new ApiApplicationDriver(
                    context.getServer(),
                    context.getAccounts(),
                    context.getAliasTable());

            driver.login(context.getAccounts().ensureAccountExists(user.getNickName()));

            LOGGER.fine(String.format("%s: %s Starting", user.getNickName(), action.toString()));

            action.execute(driver);

            LOGGER.fine(String.format("%s: %s Completed.", user.getNickName(), action.toString()));

        } catch (AssertionError | ConfigurationError | IllegalStateException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("%s: %s Failed [%s]",
                    user.getNickName(), action.toString(), e.getMessage()), e);
        } finally {
            CapacityTest.CONCURRENT_USERS.dec();
        }    
    }
}
