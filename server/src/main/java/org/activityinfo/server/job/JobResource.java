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
package org.activityinfo.server.job;

import com.google.appengine.api.taskqueue.*;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import org.activityinfo.json.JsonParser;
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
@Path("/resources/jobs")
public class JobResource {

    private static final Logger LOGGER = Logger.getLogger(JobResource.class.getName());

    private static final JsonParser PARSER = new JsonParser();
    public static final String QUEUE_NAME = "user-jobs";
    public static final String JOB_KEY_PARAM = "key";

    private Provider<AuthenticatedUser> user;
    private Queue queue;

    @Inject
    public JobResource(Provider<AuthenticatedUser> user) {
        this(user, QueueFactory.getQueue(QUEUE_NAME));
    }

    @VisibleForTesting
    JobResource(Provider<AuthenticatedUser> user, Queue queue) {
        this.user = user;
        this.queue = queue;
    }

    /**
     * Starts a new long running job
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response start(String json) {

        if(user.get().isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JobRequest request;
        try {
            request = JobRequest.fromJson(PARSER.parse(json));
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // now create the job record in the datastore
        JobEntity record = new JobEntity(user.get().getUserId());
        record.setType(request.getDescriptor().getType());
        record.setDescriptor(request.getDescriptor().toJson().toJson());
        record.setState(JobState.STARTED);
        record.setStartTime(new Date());
        record.setLocale(user.get().getUserLocale());

        Key<JobEntity> key = JobStore.ofy().save().entity(record).now();

        // And launch a task to execute the job
        TaskHandle taskHandle = queue.add(TaskOptions.Builder.withUrl(JobTaskServlet.END_POINT)
                .retryOptions(RetryOptions.Builder.withTaskRetryLimit(4))
                .param(JOB_KEY_PARAM, key.toWebSafeString()));

        LOGGER.info("Starting task " + request.getDescriptor().getType());


        return Response.ok(Response.Status.CREATED).entity(buildStatus(record).toJsonObject().toJson()).build();
    }

    /**
     * Retrieves the status of a long-running job
     */
    @GET
    @Path("{jobId}")
    public Response get(@PathParam("jobId") String jobId) {

        if(user.get().isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JobEntity job = JobStore.getUserJob(jobId).now();
        if(job == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if(job.getUserId() != user.get().getUserId()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        JobStatus status = buildStatus(job);
        return Response.ok().entity(status.toJsonObject().toJson()).build();
    }

    private JobStatus buildStatus(JobEntity job) {
        return new JobStatus(JobStore.getWebSafeKeyString(job),
                job.parseDescriptor(),
                job.getState(),
                job.parseResult(),
                job.parseError());
    }
}
