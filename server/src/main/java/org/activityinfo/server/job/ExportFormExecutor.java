package org.activityinfo.server.job;

import com.google.inject.Inject;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.store.spi.FormCatalog;

public class ExportFormExecutor implements JobExecutor<ExportFormJob, ExportResult> {

    private FormCatalog catalog;

    @Inject
    public ExportFormExecutor(FormCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public ExportResult execute(ExportFormJob descriptor) {
        return new ExportResult("http://example.com/result");
    }
}
