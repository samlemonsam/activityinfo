/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import com.google.common.base.Optional;
import com.sun.jersey.api.view.Viewable;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.login.LoginController;
import org.activityinfo.store.query.UsageTracker;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
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
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages an oAuth Flow to authenticate a user
 */
@Path("/oauth")
public class HumanitarianId {

    private static final String CLIENT_ID_PROPERTY = "oauth.hid.clientId";
    private static final String CLIENT_SECRET_PROPERTY = "oauth.hid.clientSecret";

    private static final Logger LOGGER = Logger.getLogger(HumanitarianId.class.getName());
    
    private static final String HID_REDIRECT_PATH = "/authorized2/1";
    
    private static final String STATE = "12345";
    
    private final Provider<EntityManager> entityManager;
    private final AuthTokenProvider authTokenProvider;
    
    private final Optional<AuthorizationCodeFlow> flow;

    @Inject
    public HumanitarianId(DeploymentConfiguration config,
                          Provider<EntityManager> entityManager, 
                          AuthTokenProvider authTokenProvider) throws IOException {
        this.entityManager = entityManager;
        this.authTokenProvider = authTokenProvider;

        // Load the client id and secret from the deployment configuration
        String clientId = config.getProperty(CLIENT_ID_PROPERTY);
        String clientSecret = config.getProperty(CLIENT_SECRET_PROPERTY);
    
        if(clientId == null || clientSecret == null) {
            LOGGER.info(String.format(
                "Configuration properties '%s' or '%s' are missing, humanitarian.id disabled.", 
                    CLIENT_ID_PROPERTY, CLIENT_SECRET_PROPERTY));
            
            flow = Optional.absent();
            
        } else {

            ClientParametersAuthentication clientAuthorization =
                    new ClientParametersAuthentication(
                            clientId.trim(),
                            clientSecret.trim());

            // Use the AppEngine UrlFetch service for http access
            HttpTransport httpTransport = new UrlFetchTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            GenericUrl tokenServerUrl = new GenericUrl("https://auth.humanitarian.id/oauth/authorize");
            String authorizationServerEncodedUrl = "https://auth.humanitarian.id/oauth/authorize";
            DataStore<StoredCredential> datastore = AppEngineDataStoreFactory.getDefaultInstance().getDataStore("humanitarianid");

            flow = Optional.of(new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    httpTransport,
                    jsonFactory,
                    tokenServerUrl,
                    clientAuthorization,
                    clientId,
                    authorizationServerEncodedUrl)
                    .setCredentialDataStore(datastore)
                    .build());

            LOGGER.info(String.format("Humanitarian.id authentication enabled with client id '%s'", 
                    clientAuthorization.getClientId()));
        }
    }

    /**
     * Starts the authentication process by redirecting to the humanitarian id
     */
    @GET
    @Path("/oauthconnector_hid_oauth")
    public Response beginLogin(@Context UriInfo requestUri) throws URISyntaxException {
        
        if(!flow.isPresent()) {
            return serviceUnvailable();
        }
     
        String redirectUri = requestUri.getBaseUriBuilder()
                .path(HumanitarianId.class)
                .path(HID_REDIRECT_PATH)
                .build().toASCIIString();

        String authorizationUrl = flow.get().newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setScopes(Collections.singleton("profile"))
                .setState(STATE)
                .build();
        
        return Response.status(302).location(new URI(authorizationUrl)).build();
    }
    
    @GET
    @Path(HID_REDIRECT_PATH)
    public Response humanitarianIdAuthorized(@Context UriInfo uriInfo,
                                             @QueryParam("code") String code,
                                             @QueryParam("state") String state) throws IOException {

        HumanitarianIdAccount account;
        try {

            if(!flow.isPresent()) {
                throw new IllegalStateException("Flow is not present");
            }

            // First exchange our authorization code for an access token that we can use
            // to request information about the user that has just logged in
            TokenResponse tokenResponse = flow.get().newTokenRequest(code)
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
            account = HumanitarianIdAccount.parse(response.getContent());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve authorizaton from Humanitarian.id");
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new Viewable("/page/HumanitarianIdFailure.ftl"))
                    .build();
        }
        
        // Now lookup the user's email address in our directory to see if this user already has an account
        List<User> existingUser = entityManager.get().createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", account.getEmail())
                .getResultList();

        if(existingUser.isEmpty()) {
            // If the user doesn't have an account, create one directly,
            // we are trusting humanitarian.id to verify the user's email address
            return createNewAccount(uriInfo, account);
        
        } else {

            return redirectToApp(uriInfo, existingUser.get(0));
        }
    }

    private Response serviceUnvailable() {
        return Response
                .status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("Integration with humanitarian.id is not enabled.")
                .build();
    }

    private Response createNewAccount(UriInfo baseUri, HumanitarianIdAccount account) {

        entityManager.get().getTransaction().begin();
        
        User user = new User();
        user.setEmail(account.getEmail());
        user.setName(account.getName());
        user.setDateCreated(new Date());
        user.setLocale("en");

        entityManager.get().persist(user);
        entityManager.get().getTransaction().commit();

        return redirectToApp(baseUri, user);
    }

    private Response redirectToApp(UriInfo baseUri, User user) {
        UsageTracker.track(user.getId(), "login/humanitarian_id");

        return LoginController.loginAndRedirectToApp(authTokenProvider, baseUri, user);
    }
}
