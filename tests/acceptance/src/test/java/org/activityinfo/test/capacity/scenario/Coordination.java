package org.activityinfo.test.capacity.scenario;

import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.DatabaseBuilder;
import org.activityinfo.test.capacity.agent.Agent;
import org.activityinfo.test.capacity.agent.SyncOffline;
import org.activityinfo.test.driver.ApiApplicationDriver;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.activityinfo.test.driver.Property.property;

/**
 * Models the state of a country-wide coordination effort using 
 * ActivityInfo, with administrator agents, users, etc
 */
public class Coordination {

    private static final Logger LOGGER = Logger.getLogger(Coordination.class.getName());

    private static final int FORM_COUNT = 10;
    private static final int FIELD_COUNT = 30;

    private Scenario scenario;
    private Agent administrator;

    private Random random = new Random();

    private List<String> databases = Lists.newArrayList();
    private Map<String, Partner> organizations = new HashMap<>();

    public Coordination(Scenario scenario) {
        this.scenario = scenario;
        this.administrator = scenario.newAgent("Administrator");
    }


    public void createDatabases() throws Exception {

        DatabaseBuilder builder = new DatabaseBuilder(administrator);
        databases.addAll(builder.createDatabases(15));

    }

    public void createPartnerWithUsers(int userCount) throws Exception {

        Partner partner = new Partner(nextPartnerName());
        for(int i=0;i!=userCount;++i) {
            partner.addUser(scenario.newAgent("reporting@" + partner.getDomain()));
        }
        organizations.put(partner.getName(), partner);

    }

    public void addAllUsers() throws Exception {
        ApiApplicationDriver driver = administrator.getDriver();
        for(String database : databases) {
            for(Partner partner : organizations.values()) {
                driver.addPartner(partner.getName(), database);
                for(Agent agent : partner.getUsers()) {
                    driver.grantPermission(
                            property("user", agent.getName()),
                            property("database", database),
                            property("partner", partner.getName()),
                            property("permissions", Arrays.asList("View", "View All", "Edit")));
                }
            }
            driver.flush();
        }
    }

    private String nextPartnerName() {
        char[] name = new char[3];
        do {
            name[0] = (char)('A' + random.nextInt(26));
            name[1] = (char)('A' + random.nextInt(26));
            name[2] = (char)('A' + random.nextInt(26));
        } while(organizations.containsKey(new String(name)));

        return new String(name);
    }


    public void addPartners() throws Exception {

        administrator.getDriver().startBatch();
        scenario.getAccounts().setBatchingEnabled(true);
        // Now add users 
        for(int i=0;i<10;++i) {
            createPartnerWithUsers(5);
            createPartnerWithUsers(10);
            createPartnerWithUsers(25);
        }
        scenario.getAccounts().flush();

        addAllUsers();

        administrator.getDriver().submitBatch();
    }

    public List<Agent> sampleUsers(int count) {
        List<Agent> users = Lists.newArrayList();
        for(Partner partner : organizations.values()) {
            users.addAll(partner.getUsers());
        }

        Set<Integer> sample = new HashSet<>();
        while(sample.size() < count) {
            int index = random.nextInt(users.size()-1);
            sample.add(index);
        }

        List<Agent> sampledUsers = Lists.newArrayList();
        for(Integer index : sample) {
            sampledUsers.add(users.get(index));
        }
        return sampledUsers;
    }

    public void usersEnableOfflineMode(List<Agent> agents) throws InterruptedException {
        // Ensure the admin  can sync offline
        ExecutorService executorService = Executors.newFixedThreadPool(agents.size());
        for(Agent agent : agents) {
            executorService.submit(new SyncOffline(agent));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOGGER.severe("Interrupted while waiting for users to complete synchronization");
        }
        executorService.shutdown();
    }


    public Agent getAdministrator() {
        return administrator;
    }

}
