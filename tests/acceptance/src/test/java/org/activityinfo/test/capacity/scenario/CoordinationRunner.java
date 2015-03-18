package org.activityinfo.test.capacity.scenario;


import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Models an ActivityInfo deployment throughout a single country
 * but with tens of organizations, many databases, and intensive reporting periods
 */
public class CoordinationRunner implements Runnable {

    private Scenario scenario;

    private Logger LOGGER = Logger.getLogger(CoordinationRunner.class.getName());
    

    public CoordinationRunner(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public void run() {
        
        try {
            Coordination deployment = new Coordination(scenario);
            
            // Begins by a single user setting up a database
            LOGGER.info("Starting database setup...");
            deployment.createDatabases();

            deployment.usersEnableOfflineMode(Arrays.asList(deployment.getAdministrator()));

            LOGGER.info("Inviting users to access the database");
            deployment.addPartners();
            
            deployment.usersEnableOfflineMode(deployment.sampleUsers(2));
            deployment.usersEnableOfflineMode(deployment.sampleUsers(5));
            deployment.usersEnableOfflineMode(deployment.sampleUsers(10));


        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Scenario failed with exception:" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        
    }
}
