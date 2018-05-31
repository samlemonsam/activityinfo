package org.activityinfo.server.pipeline;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.pipeline.AdditionJobDescriptor;
import org.activityinfo.model.pipeline.PipelineJobDescriptor;
import org.activityinfo.model.pipeline.PipelineJobRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class
})
public class PipelineResourceTest {

    private transient LocalServiceTestHelper helper;
    private Closeable objectifyCloseable;

    @Inject
    private Provider<EntityManager> entityManager;

    public PipelineResourceTest() {
        LocalTaskQueueTestConfig taskQueueConfig = new LocalTaskQueueTestConfig();
        taskQueueConfig.setCallbackClass(TestingTaskQueueCallback.class);
        taskQueueConfig.setDisableAutoTaskExecution(false);
        taskQueueConfig.setShouldCopyApiProxyEnvironment(true);
        helper = new LocalServiceTestHelper(
                new LocalDatastoreServiceTestConfig(),
                taskQueueConfig,
                new LocalModulesServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        objectifyCloseable = ObjectifyService.begin();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        objectifyCloseable.close();
    }

    @Test
    public void requestJob() throws Exception {
        final org.activityinfo.json.JsonParser parser = new org.activityinfo.json.JsonParser();
        final AuthenticatedUser requester = new AuthenticatedUser("XYZ", 1, "requester@gmail.com");
        final PipelineResource resource = new PipelineResource(Providers.of(requester), new PipelineJobFactory(entityManager));

        PipelineJobDescriptor descriptor = new AdditionJobDescriptor(1,1);
        PipelineJobRequest request = new PipelineJobRequest(requester.getId(), descriptor);

        Response response = resource.start(request.toJson().toJson());
        String pipelineId = (String) response.getEntity();
    }

}