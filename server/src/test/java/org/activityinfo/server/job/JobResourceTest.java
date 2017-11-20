package org.activityinfo.server.job;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.inject.util.Providers;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.job.*;
import org.activityinfo.model.resource.ResourceId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;

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
        final org.activityinfo.json.JsonParser parser = new org.activityinfo.json.JsonParser();
        final Queue queue = QueueFactory.getDefaultQueue();
        final AuthenticatedUser user = new AuthenticatedUser("XYZ", 1, "akbertram@gmail.com");
        final JobResource resource = new JobResource(Providers.of(user), queue);


        // First request starts the job

        final String jobId = ObjectifyService.run(new Work<String>() {
            @Override
            public String run() {

                ExportFormJob exportForm = new ExportFormJob(ResourceId.valueOf("FORM1"), Collections.<ExportColumn>emptyList());
                JobRequest request = new JobRequest(exportForm, "en");


                Response response = resource.start(request.toJsonObject().toJson());

                JsonValue resultObject = parser.parse((String) response.getEntity());

                JobStatus result = JobStatus.fromJson(resultObject);

                assertThat(result.getState(), equalTo(JobState.STARTED));
                return result.getId();
            }
        });

        // Second request retrieves status
        ObjectifyService.run(new VoidWork() {
            @Override
            public void vrun() {

                Response statusResponse = resource.get(jobId);
                JsonValue statusObject = parser.parse(((String) statusResponse.getEntity()));
                JobStatus status = JobStatus.fromJson(statusObject);

                assertThat(status.getState(), equalTo(JobState.STARTED));
            }
        });

    }
}