package org.activityinfo.test.acceptance;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
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
import org.codehaus.jackson.node.ObjectNode;
import org.yaml.snakeyaml.nodes.Node;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@ScenarioScoped
public class JsonApiSteps {

    private Client client;
    
    @Inject
    private Accounts accounts;
    
    @Inject
    private Server server;
    
    @Inject
    private AliasTable aliasTable;
    
    private Scenario scenario;
    
    private WebResource root;
    private ObjectMapper objectMapper;
    private PsuedoJsonParser jsonParser;

    private ApiResponse response;
    
    @Inject
    private Placeholders placeholders;
    
    
    @Before
    public void setUpClient(Scenario scenario) {
        
        this.scenario = scenario;
        
        this.client = new Client();
        this.objectMapper = new ObjectMapper();
        this.jsonParser = new PsuedoJsonParser(objectMapper);
        
        UserAccount account = accounts.any();
        client.addFilter(new HTTPBasicAuthFilter(account.getEmail(), account.getPassword()));

        root = client.resource(server.getRootUrl());
    }

    @When("^I execute the command:$")
    public void I_execute_the_command(String requestBody) throws Throwable {
        
        JsonNode request = placeholders.resolve(jsonParser.parse(requestBody));
        
        System.out.println(request.toString());
        
        recordResponse(root.path("/command")
                .entity(request.toString(), MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class));
    }

    @When("^I request (.*)$")
    public void I_request(String path) throws Throwable {
        WebResource resource = root.path(placeholders.resolvePath(path));

        scenario.write(String.format("<code><pre>GET %s</pre></code>", resource.getURI().toString()));

        recordResponse(resource
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class));

    }
    
    @Then("^the response should have status code (\\d+)$")
    public void the_response_should_have_status_code(int statusCode) throws Throwable {
        response.assertStatusCodeIs(statusCode);
    }

    @Then("^the response should be:$")
    public void the_response_should_be(String expectedResponse) throws Throwable {
        JsonNode expected = jsonParser.parse(expectedResponse);
        JsonNode actual = response.getJson();
        
        System.out.println(actual.toString());

        new JsonChecker(placeholders).check(expected, actual);
        
    }

    @Then("^the response should include:$")
    public void the_response_should_include(String expectedResponse) throws Throwable {
        throw new PendingException();
    }

    private void recordResponse(ClientResponse response) {
        this.response = new ApiResponse(response, objectMapper);
    }

    private String prettyPrint(JsonNode node) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }
}
