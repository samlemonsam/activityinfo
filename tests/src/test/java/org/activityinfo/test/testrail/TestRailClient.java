package org.activityinfo.test.testrail;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import org.activityinfo.test.config.ConfigProperty;

import javax.ws.rs.core.MediaType;

/**
 * Access the test rail API
 */
public class TestRailClient {

    private static final String ROOT_URL = "https://bdd.testrail.com/index.php?api/v2/";

    private static final int PROJECT_ID = 1;

    private static final ConfigProperty TEST_RAIL_USER = new ConfigProperty("testRailUser",
        "The user name with which to login");

    private static final ConfigProperty TEST_RAIL_PASSWORD = new ConfigProperty("testRailPassword",
        "The user name with which to login");

    private final Client client;

    public TestRailClient() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        client = Client.create(clientConfig);
        client.addFilter(new HTTPBasicAuthFilter(TEST_RAIL_USER.get(), TEST_RAIL_PASSWORD.get()));
    }

    public TestRun newRun(NewTestRun newTestRun) {
        return client.resource(ROOT_URL + "/add_run/" + PROJECT_ID)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(TestRun.class, newTestRun);
    }

    public void addResults(int testRunId, TestResults testResults) {
        ClientResponse response = client.resource(ROOT_URL + "/add_results/" + testRunId)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(ClientResponse.class, testResults);

        if(response.getStatus() != 200) {
            throw new RuntimeException("Failed to update test results: " + response.getStatus());
        }
    }
}
