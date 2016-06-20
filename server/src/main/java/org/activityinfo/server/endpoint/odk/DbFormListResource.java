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

/**
 * Created by yuriyz on 6/16/2016.
 */
@Path("/db")
public class DbFormListResource {

    private final FormLister formLister;

    @Inject
    public DbFormListResource(OdkAuthProvider authProvider, DispatcherSync dispatcher) {
        this.formLister = new FormLister(authProvider, dispatcher);
    }

    @GET
    @Path("/{dbId}/formList")
    @Produces(MediaType.TEXT_XML)
    public Response dbFormList(@Context UriInfo uri, @PathParam("dbId") int dbId) throws Exception {
        return formLister.formList(uri, Optional.of(dbId));
    }

}
