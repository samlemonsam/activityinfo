package org.activityinfo.server.job;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.activityinfo.model.job.ExportFormJob;

/**
 * Creates Jobs based on their type id
 */
public class ExecutorFactory {

    private Injector injector;

    @Inject
    public ExecutorFactory(Injector injector) {
        this.injector = injector;
    }

    public JobExecutor create(String type) {
        if(type.equals(ExportFormJob.TYPE)) {
            return injector.getInstance(ExportFormExecutor.class);
        }
        throw new IllegalArgumentException("No such type " + type);
    }
}
