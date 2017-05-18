package org.activityinfo.server.job;

import com.google.inject.Inject;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.ExportColumn;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.server.endpoint.odk.ResourceLocatorSync;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.xlsform.XlsColumnSetWriter;

import java.io.IOException;
import java.io.OutputStream;

public class ExportFormExecutor implements JobExecutor<ExportFormJob, ExportResult> {

    private final StorageProvider storageProvider;
    private ResourceLocatorSync resourceLocator;


    @Inject
    public ExportFormExecutor(ResourceLocatorSync resourceLocator, StorageProvider storageProvider) {
        this.resourceLocator = resourceLocator;
        this.storageProvider = storageProvider;
    }

    @Override
    public ExportResult execute(ExportFormJob descriptor) throws IOException {

        FormTree tree = resourceLocator.getFormTree(descriptor.getFormId());
        ColumnSet columnSet = queryColumns(descriptor);

        GeneratedResource export = storageProvider.create(XlsColumnSetWriter.EXCEL_MIME_TYPE, "Export.xls");

        XlsColumnSetWriter writer = new XlsColumnSetWriter();
        writer.addSheet(tree, columnSet);

        try(OutputStream out = export.openOutputStream()) {
            writer.write(out);
        }

        return new ExportResult(export.getDownloadUri());
    }

    private ColumnSet queryColumns(ExportFormJob descriptor) {
        QueryModel model = new QueryModel(descriptor.getFormId());
        for (ExportColumn exportColumn : descriptor.getColumns()) {
            model.selectExpr(exportColumn.getFormula()).as(exportColumn.getFormula());
        }

        return resourceLocator.query(model);

    }
}
