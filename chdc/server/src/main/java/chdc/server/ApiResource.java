package chdc.server;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.server.ApiBackend;
import org.activityinfo.store.server.FormResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Serves the API endpoints
 */
@Path("/resources")
public class ApiResource {

    private ApiBackend backend = new ChdcApiBackend();

    @Path("form/{id}")
    public FormResource getForm(@PathParam("id") String formId) {
        return new FormResource(backend, ResourceId.valueOf(formId));
    }

    @GET
    @Path("hello")
    public String hello() {
        return "Hello World";
    }

}
