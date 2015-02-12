package org.activityinfo.test.acceptance.json;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.ClientResponse;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Wrapper around a ClientResponse to simplify accessing the entity multiple times.
 */
public class ApiResponse {

    private final ClientResponse response;
    private ObjectMapper objectMapper;
    private String responseEntity;
    
    public ApiResponse(ClientResponse response, ObjectMapper objectMapper) {
        this.response = response;
        this.objectMapper = objectMapper;
    }
    
    public boolean isSuccess() {
        return response.getClientResponseStatus().getFamily() == Response.Status.Family.SUCCESSFUL;
    }
    
    public JsonNode getJson() throws IOException {
        if(responseEntity == null) {
            responseEntity = response.getEntity(String.class);
        }
        return objectMapper.readTree(responseEntity);
    }

    public void assertStatusCodeIs(int statusCode) {
        if(response.getStatus() != statusCode) {
            throw new AssertionError(String.format(
                    "Expected response with status code %d, actual status code was %d.\n%s",
                    statusCode,
                    response.getStatus(),
                    response.getEntity(String.class)));
        }
    }

}

