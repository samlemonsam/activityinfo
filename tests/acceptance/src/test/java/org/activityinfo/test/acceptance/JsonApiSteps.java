package org.activityinfo.test.acceptance;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jna.platform.win32.Netapi32Util;
import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.acceptance.json.ApiResponse;
import org.activityinfo.test.acceptance.json.JsonChecker;
import org.activityinfo.test.acceptance.json.Placeholders;
import org.activityinfo.test.acceptance.json.PsuedoJsonParser;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@ScenarioScoped
public class JsonApiSteps {
    
    @Inject
    private Accounts accounts;
    
    @Inject
    private Server server;
    
    @Inject
    private AliasTable aliasTable;
    
    private Scenario scenario;
    
    private ObjectMapper objectMapper;
    private PsuedoJsonParser jsonParser;

    private ApiResponse response;
    
    @Inject
    private Placeholders placeholders;
    private UserAccount currentAccount;


    @Before
    public void setUpClient(Scenario scenario) {
        
        this.scenario = scenario;
        
    
        this.objectMapper = new ObjectMapper();
        this.jsonParser = new PsuedoJsonParser(objectMapper);

        this.currentAccount = accounts.any();
    }

    @When("^I execute the command:$")
    public void I_execute_the_command(String requestBody) throws Throwable {
        execute(currentAccount, requestBody);
    }

    @When("([^ ]+) executes the command:$")
    public void user_executes_the_command(String accountEmail, String requestBody) throws Throwable {
        UserAccount user = accounts.ensureAccountExists(accountEmail);
        
        execute(user, requestBody);
    }

    private void execute(UserAccount account, String requestBody) {
        JsonNode request = placeholders.resolve(jsonParser.parse(requestBody));

        System.out.println(request.toString());

        recordResponse(root(account)
                .path("/command")
                .entity(request.toString(), MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class));
    }

    @When("^I request (.*)$")
    public void I_request(String path) throws Throwable {
        WebResource resource = root(currentAccount).path(placeholders.resolvePath(path));

        scenario.write(String.format("<code><pre>GET %s</pre></code>", resource.getURI().toString()));

        recordResponse(resource
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class));

    }
    
    @Then("^the response should be (\\d+) ([A-Za-z ]+)$")
    public void the_response_should_have_status_code(int statusCode, String statusCodeName) throws Throwable {
        assertConsistentStatusArguments(statusCode, statusCodeName);

        response.assertStatusCodeIs(statusCode);
        
        if(statusCode == Response.Status.NO_CONTENT.getStatusCode()) {
            response.assertBodyIsEmpty();
        }
    }

    @Then("^the response should be:$")
    public void the_response_should_be(String expectedResponse) throws Throwable {
        JsonNode expected = jsonParser.parse(expectedResponse);
        JsonNode actual = response.getJson();
        
        System.out.println(actual.toString());

        new JsonChecker(placeholders).check(expected, actual);
        
    }

    @Then("^the response should fail with (\\d+) ([A-Za-z ]+) and mention \"([^\"]*)\"$")
    public void the_response_should_fail_with_Bad_Request_and_mention(int statusCode,
                                                                      String statusPhrase, 
                                                                      String errorMessage) throws Throwable {
        assertConsistentStatusArguments(statusCode, statusPhrase);
        
        response.assertStatusCodeIs(statusCode);
        response.assertErrorMessageContains(errorMessage);

    }

    private WebResource root(UserAccount account) {
        Client client = new Client();
        client.addFilter(new HTTPBasicAuthFilter(account.getEmail(), account.getPassword()));

        return client.resource(server.getRootUrl());
    }


    private void recordResponse(ClientResponse response) {
        this.response = new ApiResponse(response, objectMapper);
    }

    private void assertConsistentStatusArguments(int statusCode, String statusCodeName) {
        Response.Status status = Response.Status.fromStatusCode(statusCode);
        if(!status.getReasonPhrase().equalsIgnoreCase(statusCodeName)) {
            throw new IllegalArgumentException(String.format("Status code/name mismatch. Did you mean %d %s",
                    statusCode, status.getReasonPhrase()));
        }
    }

}
