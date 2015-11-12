package org.activityinfo.server.authentication;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.service.DeploymentConfiguration;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

/**
 * Manages an oAuth Flow to authenticate a user
 */
@Path("/oauth")
public class HumanitarianId {

    public static final String HID_REDIRECT_PATH = "/authorized2/1";
    
    public static final String STATE = "12345";
    
    private final AuthorizationCodeFlow flow;

    @Inject
    public HumanitarianId(DeploymentConfiguration config) throws IOException {

        // Confgure the oAuth client library
        
        // Use the AppEngine UrlFetch service for http access
        HttpTransport httpTransport = new UrlFetchTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        String clientId = config.getProperty("oauth.hid.clientId");
        String clientSecret = config.getProperty("oauth.hid.clientSecret");
        GenericUrl tokenServerUrl = new GenericUrl("https://auth.humanitarian.id/oauth/authorize");
        ClientParametersAuthentication clientAuthorization = new ClientParametersAuthentication(clientId, clientSecret);
        String authorizationServerEncodedUrl = "https://auth.humanitarian.id/oauth/authorize";
        DataStore<StoredCredential> datastore = AppEngineDataStoreFactory.getDefaultInstance().getDataStore("humanitarianid");

        flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                httpTransport,
                jsonFactory,
                tokenServerUrl,
                clientAuthorization,
                clientId,
                authorizationServerEncodedUrl)
                .setCredentialDataStore(datastore)
                .build();
    }

    /**
     * Starts the authentication process by redirecting to the humanitarian id
     * @return
     */
    @GET
    @Path("/oauthconnector_hid_oauth")
    public Response beginLogin(@Context UriInfo requestUri) throws URISyntaxException {
     
        String redirectUri = requestUri.getBaseUriBuilder()
                .path(HumanitarianId.class)
                .path(HID_REDIRECT_PATH)
                .build().toASCIIString();

        String authorizationUrl = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri.toString())
                .setState(STATE)
                .build();
        
        return Response.status(302).location(new URI(authorizationUrl)).build();
    }
    
    @GET
    @Path(HID_REDIRECT_PATH)
    public String humanitarianIdAuthorized(@Context UriInfo uriInfo, 
                                           @QueryParam("code") String code,
                                           @QueryParam("state") String state) throws IOException {

        
        // First exchange our authorization code for an access token that we can use 
        // to request information about the user that has just logged in 
        TokenResponse tokenResponse = flow.newTokenRequest(code)
                .setScopes(Collections.singletonList("profile"))
                .setGrantType("authorization_code")
                .setTokenServerUrl(new GenericUrl("https://auth.humanitarian.id/oauth/access_token"))
                .execute();

        // Now query the user's email address and name
        URL accountUrl = UriBuilder.fromUri("https://auth.humanitarian.id/account.json")
                .queryParam("access_token", tokenResponse.getAccessToken())
                .build().toURL();
        
        URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse response = fetchService.fetch(accountUrl);

        JsonParser jsonParser = new JsonParser();
        JsonObject account = (JsonObject)jsonParser.parse(new String(response.getContent(), Charsets.UTF_8));
        
        // Just dump the user for right now
        return account.get("email").getAsString();
    }
}
