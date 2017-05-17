package org.activityinfo.server.job;

import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;


public interface JobExecutor<T extends JobDescriptor<R>, R extends JobResult> {

    R execute(T descriptor);
}
