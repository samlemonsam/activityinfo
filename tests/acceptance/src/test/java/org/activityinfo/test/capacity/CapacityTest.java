package org.activityinfo.test.capacity;


import com.google.common.io.Resources;
import org.activityinfo.test.capacity.scenario.CoordinationRunner;
import org.activityinfo.test.capacity.scenario.Scenario;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.Server;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Runs a series of test against the staging server
 */
public class CapacityTest {

    private static final Logger LOGGER = Logger.getLogger(CapacityTest.class.getName());
    
    public static void main(String[] args) throws IOException {
        setupLogging();

    
        Server server = new Server();
        DevServerAccounts accounts = new DevServerAccounts();

        Metrics.start();

        LOGGER.info("Running capacity tests against " + server.getRootUrl());

        Scenario scenario = new Scenario(server, accounts);
        CoordinationRunner deployment = new CoordinationRunner(scenario);
        deployment.run();

        System.out.println();
        System.out.println("Results");



        if(Metrics.ERRORS.getCount() > 0) {
            System.exit(-1);
        }
    }

    private static void setupLogging() throws IOException {
        // Divert System.err because webDriver just dumps everything without synchronization
       // System.setErr(new PrintStream(new NullOutputStream()));


        try(InputStream in = Resources.getResource(CapacityTest.class, "logging.properties").openStream()) {
            LogManager.getLogManager().readConfiguration(in);
        }
    }
}
