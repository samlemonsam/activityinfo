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
package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.promise.Promise;


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
