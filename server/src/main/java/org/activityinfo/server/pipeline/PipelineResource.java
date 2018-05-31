package org.activityinfo.server.pipeline;

import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.json.JsonParser;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.pipeline.PipelineJobDescriptor;
import org.activityinfo.model.pipeline.PipelineJobRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/pipeline")
public class PipelineResource {

    private static final Logger LOGGER = Logger.getLogger(PipelineResource.class.getName());
    private static final JsonParser PARSER = new JsonParser();

    private Provider<AuthenticatedUser> user;
    private PipelineJobFactory factory;

    private final PipelineService pipeline = PipelineServiceFactory.newPipelineService();

    @Inject
    public PipelineResource(Provider<AuthenticatedUser> user,  PipelineJobFactory factory) {
        this.user = user;
        this.factory = factory;
    }

    /**
     * Starts a new pipeline job of the specified type
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response start(String json) {

        if (user.get().isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        PipelineJobRequest request;
        try {
            request = PipelineJobRequest.fromJson(PARSER.parse(json));
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (conflicts(request)) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        // Create request record and store
        // Start pipeline job
        String pipelineKey = startPipelineJob(request.getJobDescriptor());

        LOGGER.info(() -> "Starting pipeline job " + request.getJobDescriptor().getType() + " for user " + request.getRequesterId());

        return Response.ok(Response.Status.CREATED).entity(pipelineKey).build();
    }

    private boolean conflicts(PipelineJobRequest request) {
        // Get requester id
        // Check store for user's requested jobs
        // If an already requested job is fo same type, check for conflicts
        return false;
    }

    private String startPipelineJob(PipelineJobDescriptor descriptor) {
        return factory.create(descriptor.getType(), descriptor, pipeline);
    }

}
