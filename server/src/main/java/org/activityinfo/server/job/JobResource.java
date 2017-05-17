package org.activityinfo.server.job;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.JsonParser;
import com.googlecode.objectify.Key;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.job.JobRequest;
import org.activityinfo.model.job.JobState;
import org.activityinfo.model.job.JobStatus;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Logger;

/*
 * REST endpoint for a user's scheduled jobs
 */
public class JobResource {

    private static final Logger LOGGER = Logger.getLogger(JobResource.class.getName());

    private static final JsonParser PARSER = new JsonParser();
    public static final String QUEUE_NAME = "user-tasks";
    public static final String JOB_KEY_PARAM = "key";

    private AuthenticatedUser user;
    private Queue queue;

    public JobResource(AuthenticatedUser user, Queue queue) {
        this.user = user;
        this.queue = queue;
    }

    /**
     * Starts a new long running job
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response start(String json) {
        JobRequest request;
        try {
            request = JobRequest.fromJson(PARSER.parse(json).getAsJsonObject());
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // now create the job record in the datastore
        JobEntity record = new JobEntity(user.getUserId());
        record.setType(request.getDescriptor().getType());
        record.setDescriptor(request.getDescriptor().toJsonObject().toString());
        record.setState(JobState.STARTED);
        record.setStartTime(new Date());
        record.setLocale(user.getUserLocale());

        Key<JobEntity> key = JobStore.ofy().save().entity(record).now();

        // And launch a task to execute the job
        TaskHandle taskHandle = queue.add(TaskOptions.Builder.withUrl("/tasks/job")
                .retryOptions(RetryOptions.Builder.withTaskRetryLimit(4))
                .param(JOB_KEY_PARAM, key.toWebSafeString()));

        LOGGER.info("Starting task " + request.getDescriptor().getType());


        return Response.ok(Response.Status.CREATED).entity(buildStatus(record).toJsonObject().toString()).build();
    }

    /**
     * Retrieves the status of a long-running job
     */
    @GET
    @Path("{jobId}")
    public Response get(@PathParam("jobId") String jobId) {

        JobEntity job = JobStore.getUserJob(jobId).now();
        if(job == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if(job.getUserId() != user.getUserId()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        JobStatus status = buildStatus(job);
        return Response.ok().entity(status.toString()).build();
    }

    private JobStatus buildStatus(JobEntity job) {
        return new JobStatus(JobStore.getWebSafeKeyString(job), job.parseDescriptor(), job.getState(), job.parseResult());
    }
}
