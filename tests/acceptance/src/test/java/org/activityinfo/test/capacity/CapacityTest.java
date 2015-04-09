package org.activityinfo.test.capacity;


import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.action.ActionExecution;
import org.activityinfo.test.capacity.load.ScenarioRun;
import org.activityinfo.test.capacity.logging.CapacityTestLogging;
import org.activityinfo.test.capacity.model.Scenario;
import org.activityinfo.test.capacity.model.ScenarioContext;
import org.activityinfo.test.capacity.model.UserRole;
import org.activityinfo.test.capacity.scripts.CapacityTestScript;
import org.activityinfo.test.capacity.scripts.SiteStress;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.UserAccount;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Runs a series of test against the staging server
 */
public class CapacityTest {

    private static final int MAX_CONCURRENT_USERS = 150;

    private static final Logger LOGGER = Logger.getLogger(CapacityTest.class.getName());


    private final TestContext context;
    private final List<Scenario> scenarios = Lists.newArrayList();
    public static final double MINIMUM_SUCCESS_RATE = 98;


    public CapacityTest() {
        context = new TestContext();
    }

    private void setupScenarios(CapacityTestScript script) {
        LOGGER.info("Setting up scenarios.");
        for(Scenario scenario : script.get()) {
            addScenario(scenario);
        }
    }

    private void addScenario(Scenario scenario) {
        ScenarioContext scenarioContext = new ScenarioContext(context);
        DevServerAccounts accounts = scenarioContext.getAccounts();

        accounts.setBatchingEnabled(true);
        for (UserRole user : scenario.getUsers()) {
            UserAccount account = accounts.ensureAccountExists(user.getNickName());
            LOGGER.fine(String.format("Created User: %s: %s", account.getEmail(), user.getNickName()));
        }
        accounts.flush();
        scenarios.add(scenario);
    }

    public void run() throws InterruptedException {
        LOGGER.info("Running capacity tests against " + context.getServer().getRootUrl());


        ExecutorService scenarioExecutionService = Executors.newCachedThreadPool();
        ExecutorService userExecutorService = new ThreadPoolExecutor(10, MAX_CONCURRENT_USERS,
                5, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

        List<Callable<Void>> runs = Lists.newArrayList();
        for(Scenario scenario : scenarios) {
            runs.add(Executors.callable(new ScenarioRun(context, userExecutorService, scenario), (Void)null));
        }

        scenarioExecutionService.invokeAll(runs);

        LOGGER.info("All scenarios completed");

        scenarioExecutionService.awaitTermination(5, TimeUnit.SECONDS);
        LOGGER.info("Scenario execution service shutdown.");

        userExecutorService.awaitTermination(5, TimeUnit.SECONDS);  
        LOGGER.info("User action execution service shutdown.");
    }

    public static void main(String[] args) throws Exception {

        CapacityTestLogging.setup();
        Metrics.start();

        CapacityTestScript script = new SiteStress();

        CapacityTest capacityTest = new CapacityTest();
        capacityTest.setupScenarios(script);
        capacityTest.run();

        Metrics.stop();
        LOGGER.info("Metrics stopped.");

        CapacityTestLogging.stop();
        LOGGER.info("Logging stopped.");
        
        validate();
    }

    private static void validate() {
        
        System.out.println("RESULTS");
        System.out.println("=======");


        boolean failed = false;
        for (String actionName : ActionExecution.ACTIONS) {
            ActionExecution.ActionMetrics metrics = new ActionExecution.ActionMetrics(actionName);
            double successRate = metrics.getSuccessRate();
            System.out.printf("%s: %10.1f%% %s\n", actionName, successRate,
                    (successRate >= MINIMUM_SUCCESS_RATE ? "OK" : "FAILED"));
            
            if(successRate < MINIMUM_SUCCESS_RATE) {
                failed = true;
            }
        }
        
        if(failed) {
            System.exit(-1);
        } else {
            System.exit(0);
        }
        
    }
}
