package org.activityinfo.server.jobs;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.JobRequest;
import org.activityinfo.model.job.JobState;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.job.JobResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class JobResourceTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100),
                    new LocalTaskQueueTestConfig());

    private int userId = 1;
    private Closeable objectifyCloseable;

    @Before
    public void setUp() {
        helper.setUp();
        objectifyCloseable = ObjectifyService.begin();
    }

    @After
    public void tearDown() {
        helper.tearDown();
        objectifyCloseable.close();
    }

    @Test
    public void startJob() {

        ExportFormJob exportForm = new ExportFormJob(ResourceId.valueOf("FORM1"));
        JobRequest request = new JobRequest(exportForm, "en");

        Queue queue = QueueFactory.getDefaultQueue();
        JobResource resource = new JobResource(new AuthenticatedUser("XYZ", 1, "akbertram@gmail.com"), queue);
        Response response = resource.start(request.toJsonObject().toString());

        JsonParser parser = new JsonParser();
        JsonElement resultObject = parser.parse((String) response.getEntity());

        JobStatus result = JobStatus.fromJson(resultObject.getAsJsonObject());

        assertThat(result.getState(), equalTo(JobState.STARTED));

    }
}