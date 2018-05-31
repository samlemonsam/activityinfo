package org.activityinfo.server.pipeline.job;

import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import org.junit.Test;

public class AdditionJobTest extends PipelineJobTestCase {

    public AdditionJobTest() {
        super();
    }

    @Test
    public void testJob() throws Exception {
        PipelineService service = PipelineServiceFactory.newPipelineService();
        AdditionJob job = new AdditionJob();
        String pipelineId = service.startNewPipeline(job, 1, 1);
        JobInfo jobInfo = service.getJobInfo(pipelineId);
        JobInfo.State state = jobInfo.getJobState();
        if (JobInfo.State.COMPLETED_SUCCESSFULLY == state) {
            System.out.println("The output is " + jobInfo.getOutput());
        }
        Integer output = (Integer) waitForJobToComplete(pipelineId);
        System.out.println("The output is " + output);
        assert (output == 2);
    }
}
