package org.activityinfo.server.job;

import com.google.inject.Inject;
import org.activityinfo.analysis.pivot.viewModel.*;
import org.activityinfo.model.analysis.pivot.*;
import org.activityinfo.model.job.ExportPivotTableJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.observable.Observable;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.store.query.shared.FormSource;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ExportPivotTableExecutor implements JobExecutor<ExportPivotTableJob, ExportResult> {

    private static final String CSV_UTF8_MIME = "text/csv;charset=UTF-8";

    private final StorageProvider storageProvider;
    private final FormSource formSource;

    @Inject
    public ExportPivotTableExecutor(StorageProvider storageProvider, FormSource formSource) {
        this.storageProvider = storageProvider;
        this.formSource = formSource;
    }

    @Override
    public ExportResult execute(ExportPivotTableJob descriptor) throws IOException {
        PivotModel pivotModel = descriptor.getPivotModel();
        PivotViewModel viewModel = new PivotViewModel(Observable.just(pivotModel), formSource);
        PivotTable pivotTable = viewModel.getPivotTable().waitFor();
        GeneratedResource export = storageProvider.create(CSV_UTF8_MIME, "PivotTable.csv");

        try (OutputStreamWriter writer = new OutputStreamWriter(export.openOutputStream(), "UTF-8")) {
            writer.write(PivotTableRenderer.renderDelimited(pivotTable, ","));
        }

        return new ExportResult(export.getDownloadUri());
    }

}
