package org.activityinfo.ui.client.component.table.action;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.ActivityInfoClientAsyncImpl;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobState;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.ui.client.page.report.ExportDialog;

public class ExportJobTask implements ExportDialog.AsyncTask {

    private ActivityInfoClientAsync client;
    private JobDescriptor<ExportResult> job;

    public ExportJobTask(ActivityInfoClientAsync client, JobDescriptor<ExportResult> job) {
        this.client = client;
        this.job = job;
    }

    public ExportJobTask(JobDescriptor<ExportResult> job) {
        this.client = new ActivityInfoClientAsyncImpl();
        this.job = job;
    }



    @Override
    public void start(final AsyncCallback<ExportDialog.AsyncTaskPoller> callback) {
        client.startJob(job).then(new AsyncCallback<JobStatus<JobDescriptor<ExportResult>, ExportResult>>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(final JobStatus<JobDescriptor<ExportResult>, ExportResult> result) {
                callback.onSuccess(new ExportDialog.AsyncTaskPoller() {
                    @Override
                    public void poll(final ExportDialog.ProgressCallback pollCallback) {
                        client.getJobStatus(result.getId()).then(new AsyncCallback<JobStatus<?, ?>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                pollCallback.onFailure(caught);
                            }

                            @Override
                            public void onSuccess(JobStatus<?, ?> result) {
                                if(result.getState() == JobState.COMPLETED) {
                                    ExportResult exportResult = (ExportResult) result.getResult();
                                    pollCallback.onDownloadReady(exportResult.getDownloadUrl());
                                } else if (result.getState() == JobState.FAILED) {
                                    pollCallback.onFailure(new RuntimeException());
                                } else if (result.getState() == JobState.STARTED) {
                                    pollCallback.onProgress(0);
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
