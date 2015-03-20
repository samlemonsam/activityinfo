package org.activityinfo.test.capacity;


import com.codahale.metrics.Counter;
import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.load.ScenarioRun;
import org.activityinfo.test.capacity.logging.CapacityTestLogging;
import org.activityinfo.test.capacity.model.Scenario;
import org.activityinfo.test.capacity.model.ScenarioContext;
import org.activityinfo.test.capacity.model.UserRole;
import org.activityinfo.test.capacity.scenario.coordination.CoordinationScenario;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.UserAccount;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Runs a series of test against the staging server
 */
public class CapacityTest {
    
    private static final int MAX_CONCURRENT_USERS = 150;
    
    public static final Counter CONCURRENT_USERS = Metrics.REGISTRY.counter("concurrentUsers");
    private static final Logger LOGGER = Logger.getLogger(CapacityTest.class.getName());
    

    private final TestContext context;
    private final List<Scenario> scenarios = Lists.newArrayList();
    
    
    public CapacityTest() {
        context = new TestContext();
    }

    private void setupScenarios() {
        LOGGER.info("Setting up scenarios.");
        addScenario(new CoordinationScenario());
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

        for(Scenario scenario : scenarios) {
            scenarioExecutionService.submit(new ScenarioRun(context, userExecutorService, scenario));
        }
        
        scenarioExecutionService.shutdown();
        scenarioExecutionService.awaitTermination(10, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        CapacityTestLogging.setup();
        Metrics.start();


        CapacityTest capacityTest = new CapacityTest();
        capacityTest.setupScenarios();
        capacityTest.run();


        if (Metrics.ERRORS.getCount() > 0) {
            System.exit(-1);
        }
    }
}
