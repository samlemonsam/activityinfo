package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;


public class JobStatusRequest<T extends JobDescriptor<R>, R extends JobResult> implements HttpRequest<JobStatus<T, R>> {

    private String jobId;

    public JobStatusRequest(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public Promise<JobStatus<T, R>> execute(ActivityInfoClientAsync async) {
        return (Promise)async.getJobStatus(jobId);
    }


    @Override
    public int refreshInterval(JobStatus<T, R> result) {
        switch (result.getState()) {
            // Once the job is started, query the server
            // every 500 milliseconds for an update.
            case STARTED:
                return 500;

            // Once the job has completed or failed, there is no need
            // to check for additional updates.
            default:
            case COMPLETED:
            case FAILED:
                return -1;
        }
    }
}
