package org.activityinfo.server.job;

import com.google.inject.Inject;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.io.xls.XlsTableWriter;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.store.query.server.FormSourceSyncImpl;

import java.io.IOException;
import java.io.OutputStream;

public class ExportFormExecutor implements JobExecutor<ExportFormJob, ExportResult> {

    private final FormSourceSyncImpl formSource;
    private final StorageProvider storageProvider;


    @Inject
    public ExportFormExecutor(FormSourceSyncImpl formSource, StorageProvider storageProvider) {
        this.formSource = formSource;
        this.storageProvider = storageProvider;
    }

    @Override
    public ExportResult execute(ExportFormJob descriptor) throws IOException {

        TableModel tableModel = descriptor.getTableModel();

        GeneratedResource export = storageProvider.create(XlsTableWriter.EXCEL_MIME_TYPE, "Export.xls");

        TableViewModel viewModel = new TableViewModel(formSource, tableModel);

        EffectiveTableModel effectiveTableModel = viewModel.getEffectiveTable().waitFor();
        ColumnSet columnSet = effectiveTableModel.getColumnSet().waitFor();

        XlsTableWriter writer = new XlsTableWriter();
        writer.addSheet(effectiveTableModel, columnSet);

        try(OutputStream out = export.openOutputStream()) {
            writer.write(out);
        }

        return new ExportResult(export.getDownloadUri());
    }
}
