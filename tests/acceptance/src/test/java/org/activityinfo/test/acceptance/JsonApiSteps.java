package org.activityinfo.test.acceptance;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.sut.UserAccount;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@ScenarioScoped
public class JsonApiSteps {

    private Yaml yaml;
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
    private ClientResponse response;
    private Escaper htmlEscaper;


    private enum MatchType {
        FULL,
        PARTIAL
    }
    
    @Before
    public void setUpClient(Scenario scenario) {
        
        this.scenario = scenario;
        
        this.yaml = new Yaml();
        this.htmlEscaper = HtmlEscapers.htmlEscaper();
        this.client = new Client();
        this.objectMapper = new ObjectMapper();
        
        UserAccount account = accounts.any();
        client.addFilter(new HTTPBasicAuthFilter(account.getEmail(), account.getPassword()));

        root = client.resource(server.getRootUrl());
    }
    
    @When("^I execute the command:$")
    public void I_execute_the_command(String requestYaml) throws Throwable {
        Map<String, Object> request = readYaml(requestYaml);
        String json = objectMapper.writeValueAsString(replacePlaceholders(request));
        System.out.println(json);
        response = root.path("/command")
                .entity(json, MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
    }


    @When("^I request (.*)$")
    public void I_request(String path) throws Throwable {
        WebResource resource = root.path(replacePathPlaceholders(path));

        scenario.write(String.format("<code><pre>GET %s</pre></code>", resource.getURI().toString()));

        response = resource
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);

    }
    
    @Then("^the response should have status code (\\d+)$")
    public void the_response_should_have_status_code(int statusCode) throws Throwable {

        writeResponse();

        if(response.getStatus() != statusCode) {
            throw new AssertionError(String.format(
                    "Expected response with status code %d, actual status code was %d.\n%s", 
                        statusCode,
                        response.getStatus(),
                        response.getEntity(String.class)));
        }
        assertThat(response.getStatus(), equalTo(statusCode));
    }

    @Then("^the response should be:$")
    public void the_response_should_be(String expectedResponse) throws Throwable {
     
        writeResponse();
        
        Map<String, Object> expectedContent = readYaml(expectedResponse);
        JsonNode actualContent = readResponseAsJson();

        checkResponse(expectedContent, actualContent, MatchType.FULL);
    }


    @Then("^the response should include:$")
    public void the_response_should_include(String expectedResponse) throws Throwable {

        writeResponse();

        Map<String, Object> expectedContent = readYaml(expectedResponse);
        JsonNode actualContent = readResponseAsJson();

        System.out.println(prettyPrint(actualContent));

        checkResponse(expectedContent, actualContent, MatchType.PARTIAL);
    }

    
    private void writeResponse() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("<code>");
//        sb.append("<pre>");
//        for (Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
//            for (String value : entry.getValue()) {
//                sb.append(entry.getKey()).append(": ").append(value).append("\n");
//            }
//        }
//        sb.append("\n");
//
//        String responseText = response.getEntity(String.class);
//        try {
//            JsonNode node = objectMapper.readValue(responseText, JsonNode.class);
//            sb.append(htmlEscaper.escape(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)));
//        } catch(Exception e) {
//            sb.append(htmlEscaper.escape(responseText));
//        }
//        
//        sb.append("</pre></code>");
//        
//        scenario.write(sb.toString());

    }

    private JsonNode readResponseAsJson() throws IOException {
        if(response.getClientResponseStatus().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new AssertionError("Request failed: " + response.getEntity(String.class));
        }
        assertThat(response.getClientResponseStatus().getFamily(), equalTo(Response.Status.Family.SUCCESSFUL));

        if(!response.getType().toString().startsWith("application/json")) {
            throw new AssertionError("Expected response with Content-Type application/json");
        }

        return objectMapper.readTree(response.getEntity(String.class));
    }

    private void checkResponse(Map<String, Object> expectedContent, JsonNode actualContent, MatchType matchType) {
        if(!actualContent.isObject()) {
            throw new AssertionError("Expected a JSON object as the response, got " +
                    actualContent.asToken().name() + ":\n " +actualContent.toString());
        }

        ObjectNode actualObject = (ObjectNode)actualContent;
        if(matchType == MatchType.FULL) {
            if (actualObject.size() != expectedContent.size()) {
                throw new AssertionError("Unexpected response: " + actualContent);
            }
        }
        for(String property : expectedContent.keySet()) {
            if(!actualContent.has(property)) {
                throw new AssertionError(String.format("Missing '%s' property from response: %s", property, actualContent));
            }
            Object expectedValue = expectedContent.get(property);
            if(isPlaceholder(expectedValue)) {
                aliasTable.mapId(parseAlias(expectedValue), actualContent.get(property).asInt());
            } else {
                throw new UnsupportedOperationException();       
            }
        }
    }


    private String parseAlias(Object placeholder) {
        Preconditions.checkArgument(isPlaceholder(placeholder));
        String expr = (String)placeholder;
        if(expr.charAt(1) == '{' && expr.endsWith("}")) {
            return expr.substring(2, expr.length()-1);
        } else {
            return ((String) placeholder).substring(1);
        }
    }

    private boolean isPlaceholder(Object value) {
        if(value instanceof String) {
            String stringValue = (String) value;
            return stringValue.startsWith("$");
        }
        return false;
    }


    private Map<String, Object> readYaml(String requestYaml) {
        return (Map<String, Object>) yaml.loadAs(requestYaml, Map.class);
    }

    private Map<String, Object> replacePlaceholders(Map<String, Object> object) {
        Map<String, Object> output = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            output.put(entry.getKey(), replacePlaceholders(entry.getValue()));
        }
        return output;
    }

    private Object replacePlaceholders(Object value) {
        if(isPlaceholder(value)) {
            return aliasTable.getId(parseAlias(value));
        } else if(value instanceof Map) {
            return replacePlaceholders((Map<String, Object>) value);
        } else if(value instanceof Collection) {
            throw new UnsupportedOperationException();
        } else {
            return value;
        }
    }

    private String replacePathPlaceholders(String path) {
        String[] parts = path.split("/");
        List<String> evaluatedParts = new ArrayList<>();
        for(String part : parts) {
            evaluatedParts.add(replacePlaceholders(part).toString());
        }
        return Joiner.on("/").join(evaluatedParts);
    }

    private String prettyPrint(JsonNode node) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }
}
