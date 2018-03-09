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
package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.activityinfo.server.command.DispatcherSync;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/formList")
public class FormListResource {

    private final FormLister formLister;

    @Inject
    public FormListResource(OdkAuthProvider authProvider, DispatcherSync dispatcher) {
        this.formLister = new FormLister(authProvider, dispatcher);
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    public Response formList(@Context UriInfo uri) throws Exception {
        return formLister.formList(uri, Optional.<Integer>absent());
    }

    @GET
    @Path("/db/{databaseId}")
    @Produces(MediaType.TEXT_XML)
    public Response formList(@Context UriInfo uri, @PathParam("databaseId") int databaseId) throws Exception {
        return formLister.formList(uri, Optional.of(databaseId));
    }
}