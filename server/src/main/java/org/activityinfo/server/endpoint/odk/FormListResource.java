package org.activityinfo.server.endpoint.odk;

import com.google.common.collect.Maps;
import com.sun.jersey.api.view.Viewable;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.server.util.monitoring.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

@Path("/formList")
public class FormListResource extends ODKResource {
    
    
    @GET 
    @Timed("odk.form_list")
    @Produces(MediaType.TEXT_XML)
    public Response formList(@Context UriInfo info) throws Exception {
        if (enforceAuthorization()) {
            return askAuthentication();
        }
        LOGGER.finer("ODK formlist requested by " + getUser().getEmail() + " (" + getUser().getId() + ")");

        Map<String, Object> map = Maps.newHashMap();
        map.put("schema", dispatcher.execute(new GetSchema()));
        map.put("host", info.getBaseUri().toString());

        return Response.ok(new Viewable("/odk/formList.ftl", map)).build();
    }
}
