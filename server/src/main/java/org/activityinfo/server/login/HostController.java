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
package org.activityinfo.server.login;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.view.Viewable;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.csp.ContentSecurityPolicy;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.login.model.HostPageModel;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.util.HashMap;

@Path(HostController.ENDPOINT)
public class HostController {
    public static final String ENDPOINT = "/app";

    private final ServerSideAuthProvider authProvider;
    private final Provider<EntityManager> entityManager;
    private final ContentSecurityPolicy contentSecurityPolicy = new ContentSecurityPolicy();

    @Inject
    public HostController(ServerSideAuthProvider authProvider,
                          Provider<EntityManager> entityManager) {
        this.authProvider = authProvider;
        this.entityManager = entityManager;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getHostPage(@Context UriInfo uri,
                                @Context HttpServletRequest req,
                                @QueryParam("ui") String ui,
                                @QueryParam("locale") String locale,
                                @QueryParam("logging") String logging,
                                @QueryParam("gwt.codesvr") String codeServer) throws Exception {

        if (!authProvider.isAuthenticated()) {
            // Otherwise, go to the default ActivityInfo root page
            return Response.temporaryRedirect(uri.getAbsolutePathBuilder().replacePath("/login").build()).build();
        }

        User authenticatedUser = entityManager.get().find(User.class, authProvider.get().getUserId());
        if(authenticatedUser.hasFeatureFlag("v4")) {
            return Response.temporaryRedirect(UriBuilder.fromUri("https://v4.activityinfo.org/app").build())
                    .build();
        }

        String appUri = uri.getAbsolutePathBuilder().replaceQuery("").build().toString();

        HostPageModel model = new HostPageModel(appUri);


        model.setFeatureFlags(authenticatedUser.getFeatures());
        model.setNewUI("3".equals(ui) || "3dev".equals(ui));

        if("3dev".equals(ui)) {
            model.setBootstrapScript("/App/App.nocache.js");

        } else if("dev".equals(ui)) {
            // Running in development mode
            // Use the default bootstrap script
            model.setBootstrapScript("/ActivityInfo/ActivityInfo.nocache.js");
        
        } else if("true".equalsIgnoreCase(logging)) {
            // Load a special logging version of the Application
            model.setBootstrapScript("/ActivityInfoLogging/ActivityInfoLogging.nocache.js");
            
        } else {
            // Load the normal production application, based on the user's preferred 
            // locale or the one explicitly provided
            if(Strings.isNullOrEmpty(locale)) {
                locale = authProvider.get().getUserLocale();
            }
            String module;
            if(model.isNewUI()) {
                module = "App";
            } else {
                module = "ActivityInfo";
            }
            model.setBootstrapScript(String.format("/%s/%s.js", module, locale));
            model.setAppCacheManifest(String.format("/%s/%s.appcache", module, locale));
        }

        Response.ResponseBuilder response = Response
                .ok(model.asViewable())
                .type(MediaType.TEXT_HTML)
                .cacheControl(CacheControl.valueOf("no-cache"));

        contentSecurityPolicy.applyTo(response);

        return response.build();
    }

    /**
     * @return a simple error page indicating that the GWT app does not support
     * the user's browser. This is necessary because user-agent based selection
     * is done server-side when the javascript is requested, so all we can do
     * is redirect the user to this page.
     */
    @GET 
    @Path("/unsupportedBrowser")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getUnsupportedBrowserMessage() {
        return new Viewable("/page/UnsupportedBrowser.ftl", new HashMap());
    }

    @GET
    @Path("/offline")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getOfflinePage() {
        return new Viewable("/page/Offline.ftl", new HashMap<>());
    }
}
