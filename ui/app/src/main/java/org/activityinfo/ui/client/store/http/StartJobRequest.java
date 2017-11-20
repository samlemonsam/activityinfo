package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;


public class StartJobRequest<T extends JobDescriptor<R>, R extends JobResult> implements HttpRequest<JobStatus<T, R>> {

    private final T job;

    public StartJobRequest(T job) {
        this.job = job;
    }

    @Override
    public Promise<JobStatus<T, R>> execute(ActivityInfoClientAsync async) {
        return async.startJob(job);
    }


    @Override
    public int refreshInterval(JobStatus<T, R> result) {
        return -1;
    }
}
