package org.activityinfo.server.login;

import com.sun.jersey.api.view.Viewable;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.login.model.HostPageModel;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;

/**
 * Serves the main GWT application
 */
@Path("/app/{locale}")
public class AppResource {

    private String locale;
    private final ServerSideAuthProvider authProvider;
    private final GwtApp app;

    @Inject
    public AppResource(ServerSideAuthProvider authProvider, ServletContext servletContext) {
        this.locale = locale;
        this.authProvider = authProvider;
        this.app = new GwtApp(servletContext, "/ActivityInfo/");
    }

    @GET
    @Path("{locale}")
    @Produces(MediaType.TEXT_HTML)
    public Response get(@Context UriInfo uri) {
        // Verify that the user is logged in, otherwise redirect to the login page
        if(!authProvider.isAuthenticated()) {
            URI loginUrl = uri.getAbsolutePathBuilder().path("login").build();
            return Response.temporaryRedirect(loginUrl).build();
        }

        // Serve the HTML host page that 
        HostPageModel pageModel = new HostPageModel(uri.getRequestUri().toASCIIString());

        return Response.ok(pageModel.asViewable())
                .type(MediaType.TEXT_HTML)
                .cacheControl(CacheControl.valueOf("no-cache"))
                .build();
    }
    
    @GET
    @Path("{locale}/app.nocache.js")
    @Produces("application/javascript")
    public Response getScript() {
        
    }
    
}
