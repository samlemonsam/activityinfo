package org.activityinfo.test.sut;

import com.codahale.metrics.Meter;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import org.activityinfo.test.capacity.Metrics;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;


/**
 * Manages creation of users accounts on the local dev server
 */
public class DevServerAccounts implements Accounts {

    private static final Logger LOGGER = Logger.getLogger(DevServerAccounts.class.getName());


    private static final String DEV_PASSWORD = "notasecret";

    private final Meter users = Metrics.REGISTRY.meter("registeredUsers");

    /**
     * Map from test-friendly handles to unique email addresses
     */
    private LoadingCache<String, UserAccount> aliasMap;

    private Random random = new Random();
    private boolean batchingEnabled = false;
    private List<String> pendingUsers = Lists.newArrayList();
    private String locale = "en";
    private Server server;

    @Inject
    public DevServerAccounts(Server server) {
        this.server = server;
        aliasMap = CacheBuilder.newBuilder().concurrencyLevel(100).build(new CacheLoader<String, UserAccount>() {
            @Override
            public UserAccount load(String key) throws Exception {
                return createUniqueAccount(key);
            }
        });
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isBatchingEnabled() {
        return batchingEnabled;
    }

    public void setBatchingEnabled(boolean batchingEnabled) {
        this.batchingEnabled = batchingEnabled;
    }

    @Override
    public UserAccount ensureAccountExists(String userName) {
        try {
            return aliasMap.get(userName);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Creates a unique account mapped to the username for the duration of the test.
     * This ensures that each test is isolated.
     *
     * @param userName a friendly test name actually used in tests
     * @return a UserAccount with a unique username and password for this user
     */
    private synchronized UserAccount createUniqueAccount(String userName) {

        String email = generateAlias(userName);

        LOGGER.fine(String.format("Creating account %s for user %s", email, userName));

        if(batchingEnabled) {
            pendingUsers.add(email);
        } else {
            insertUsers(Arrays.asList(email));
        }

        return new UserAccount(email, DEV_PASSWORD);
    }

    public void flush() {
        if(!pendingUsers.isEmpty()) {
            LOGGER.info(String.format("Creating %d accounts...", pendingUsers.size()));
            insertUsers(pendingUsers);
            pendingUsers.clear();
        }
    }

    private void insertUsers(List<String> users) {

        Client client = Client.create();
        WebResource userResources = client.resource(server.getRootUrl()).path("resources").path("users");

        LOGGER.info("Creating new account via " + userResources);

        for(String user : users) {
            Form form = new Form();
            form.putSingle("email", user);
            form.putSingle("name", nameForEmail(user));
            form.putSingle("password", DEV_PASSWORD);
            form.putSingle("locale", locale);

            ClientResponse response = userResources.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class);
            
            if(response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                LOGGER.info("User " + user + " created " + response.getStatus());
            } else {
                throw new RuntimeException("Could not create user " + user + ": " + response.getClientResponseStatus() + "\n"
                        + response.getEntity(String.class));
            }
        }
    }

    private String generateAlias(String testHandle) {
        Preconditions.checkNotNull(testHandle, "testHandle");

        StringBuilder alias = new StringBuilder();

        // Add any alphabetic prefix from the test handle,
        // so that bob@bedatadiven.com becomes bob_xyzzsdff@mailinator.com
        // and is at least recognizable
        for(int i=0;i!=testHandle.length();++i) {
            char c = testHandle.toLowerCase().charAt(i);
            if(c >= 'a' && c <= 'z') {
                alias.append(c);
            } else {
                break;
            }
        }
        if(alias.length() == 0) {
            alias.append("user");
        }
        alias.append("_");
        alias.append(Long.toHexString(random.nextLong()));
        alias.append("@mailinator.com");

        return alias.toString();
    }
    
    private String nameForEmail(String email) {
        int at = email.indexOf('@');
        return email.substring(0, at);
    }

    @Override
    public UserAccount any() {
        return ensureAccountExists("user");
    }
}
