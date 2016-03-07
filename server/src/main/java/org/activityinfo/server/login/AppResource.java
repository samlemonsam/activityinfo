package org.activityinfo.server.login;

import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.login.model.HostPageModel;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.net.URI;

/**
 * Serves the main GWT application
 */
@Path("/app")
public class AppResource {

    private String locale;
    private final ServerSideAuthProvider authProvider;

    @Inject
    public AppResource(ServerSideAuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @GET
    @Path("{locale}")
    @Produces(MediaType.TEXT_HTML)
    public Response get(@Context UriInfo uri, @PathParam("locale") String locale) {
        // Verify that the user is logged in, otherwise redirect to the login page
        if(!authProvider.isAuthenticated()) {
            URI loginUrl = uri.getBaseUriBuilder().path("login").build();
            return Response.temporaryRedirect(loginUrl).build();
        }

        // Serve the HTML host page that 
        HostPageModel pageModel = new HostPageModel(uri.getRequestUri().toASCIIString());
        pageModel.setAppCacheManifest("/ActivityInfo/" + locale + ".appcache");
        pageModel.setBootstrapScript("/ActivityInfo/" + locale + ".js");
        
        return Response.ok(pageModel.asViewable())
                .type(MediaType.TEXT_HTML)
                .cacheControl(CacheControl.valueOf("no-cache"))
                .build();
    }
    
}
