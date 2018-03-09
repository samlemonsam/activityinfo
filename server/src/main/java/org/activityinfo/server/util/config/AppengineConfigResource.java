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
package org.activityinfo.server.util.config;

import com.google.common.collect.Maps;
import com.sun.jersey.api.view.Viewable;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

/**
 * Simple servlet to allow AppEngine administrators to define the configuration
 * properties for this instance. This makes it possible to set config params,
 * like legacy keys, etc, seperately from the (public) source code.
 * <p/>
 * <p/>
 * This servlet stores the text of a properties file to the Datastore
 */
@Path("/admin/config")
public class AppengineConfigResource {

    public static final String END_POINT = "/admin/config";

    @GET @Produces(MediaType.TEXT_HTML)
    public Viewable getPage() {
        Map<String, String> model = Maps.newHashMap();
        model.put("currentConfig", AppEngineConfig.getPropertyFile());

        return new Viewable("/page/Config.ftl", model);
    }

    @POST
    public Response update(@Context UriInfo uri, @FormParam("config") String config) {
        AppEngineConfig.setPropertyFile(config);

        return Response.seeOther(uri.getRequestUri()).build();
    }

}
