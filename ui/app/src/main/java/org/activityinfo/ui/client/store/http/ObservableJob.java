package org.activityinfo.ui.client.store.http;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobState;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;

public class ObservableJob<T extends JobDescriptor<R>, R extends JobResult> extends Observable<JobStatus<T, R>> {

    private ActivityInfoClientAsync client;
    private Promise<JobStatus<T, R>> startResult;

    private JobStatus<T, R> currentStatus;

    private Scheduler scheduler = Scheduler.get();

    public ObservableJob(ActivityInfoClientAsync client, T descriptor, Promise<JobStatus<T, R>> startResult) {
        this.client = client;
        this.startResult = startResult;
        this.startResult.then(new AsyncCallback<JobStatus<T, R>>() {
            @Override
            public void onFailure(Throwable caught) {
                // Failed to start the job - we're done.
                currentStatus = new JobStatus<T, R>(null, descriptor, JobState.FAILED, null);
            }

            @Override
            public void onSuccess(JobStatus<T, R> result) {
                currentStatus = result;
                schedulePoll();
            }
        });
    }


    private void schedulePoll() {
        scheduler.scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                poll();
                return false;
            }

        }, 500);
    }

    private void poll() {
        client.getJobStatus(currentStatus.getId()).then(new AsyncCallback<JobStatus<?, ?>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(JobStatus<?, ?> result) {

            }
        });
    }

    @Override
    public boolean isLoading() {
        return currentStatus != null;
    }

    @Override
    public JobStatus<T, R> get() {
        return currentStatus;
    }
}
