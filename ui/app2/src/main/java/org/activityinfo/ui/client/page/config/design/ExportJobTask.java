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
package org.activityinfo.ui.client.page.config.design;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.ActivityInfoClientAsyncImpl;
import org.activityinfo.model.error.ApiException;
import org.activityinfo.model.job.*;
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
                                    String error = result.getError() != null ? result.getError().toJson().toJson() : "";
                                    pollCallback.onFailure(new ApiException(error));
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
