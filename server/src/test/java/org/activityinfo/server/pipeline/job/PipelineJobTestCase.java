package org.activityinfo.server.pipeline.job;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.server.pipeline.TestingTaskQueueCallback;
import org.junit.After;
import org.junit.Before;

public class PipelineJobTestCase {

    private transient LocalServiceTestHelper helper;
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

    public PipelineJobTestCase() {
        LocalTaskQueueTestConfig taskQueueConfig = new LocalTaskQueueTestConfig();
        taskQueueConfig.setCallbackClass(TestingTaskQueueCallback.class);
        taskQueueConfig.setDisableAutoTaskExecution(false);
        taskQueueConfig.setShouldCopyApiProxyEnvironment(true);
        helper = new LocalServiceTestHelper(
                new LocalDatastoreServiceTestConfig(),
                taskQueueConfig,
                new LocalModulesServiceTestConfig());
    }


    @SuppressWarnings("unchecked")
    protected <E> E waitForJobToComplete(String pipelineId) throws Exception {
        PipelineService service = PipelineServiceFactory.newPipelineService();
        while (true) {
            Thread.sleep(2000);
            JobInfo jobInfo = service.getJobInfo(pipelineId);
            switch (jobInfo.getJobState()) {
                case COMPLETED_SUCCESSFULLY:
                    return (E) jobInfo.getOutput();
                case RUNNING:
                    break;
                case WAITING_TO_RETRY:
                    break;
                case STOPPED_BY_ERROR:
                    throw new RuntimeException("Job stopped " + jobInfo.getError());
                case STOPPED_BY_REQUEST:
                    throw new RuntimeException("Job stopped by request.");
                case CANCELED_BY_REQUEST:
                    throw new RuntimeException("Job cancelled by request.");
                default:
                    throw new RuntimeException("Unknown Job state: " + jobInfo.getJobState());
            }
        }
    }

}
