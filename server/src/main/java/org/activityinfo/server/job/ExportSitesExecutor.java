package org.activityinfo.server.job;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.io.xls.XlsTableWriter;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.FilterUrlSerializer;
import org.activityinfo.model.error.ApiError;
import org.activityinfo.model.error.ApiErrorCode;
import org.activityinfo.model.error.ApiErrorType;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.job.ExportSitesJob;
import org.activityinfo.model.error.ApiException;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.endpoint.export.ColumnSizeException;
import org.activityinfo.server.endpoint.export.SiteExporter;
import org.activityinfo.server.endpoint.export.TaskContext;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ExportSitesExecutor implements JobExecutor<ExportSitesJob, ExportResult> {

    private static final Logger LOGGER = Logger.getLogger(ExportSitesExecutor.class.getName());

    private StorageProvider storageProvider;
    private Provider<DispatcherSync> dispatcher;
    private Provider<AuthenticatedUser> authUser;

    @Inject
    public ExportSitesExecutor(StorageProvider storageProvider,
                               Provider<DispatcherSync> dispatcher,
                               Provider<AuthenticatedUser> authUser) {
        this.storageProvider = storageProvider;
        this.dispatcher = dispatcher;
        this.authUser = authUser;
    }

    @Override
    public ExportResult execute(ExportSitesJob descriptor) throws IOException {
        // Create a unique key from which the user can retrieve the file from GCS
        GeneratedResource export = storageProvider.create(XlsTableWriter.EXCEL_MIME_TYPE, fileName());

        Filter filter = FilterUrlSerializer.fromUrlFragment(descriptor.getFilter());

        LOGGER.info(() -> "Exporting Sites with filter {" + filter.toString()
                + "} for user " + authUser.get().getUserId()
                + " to export resource " + export.getId());

        // Save to Export storage
        try (OutputStream out = export.openOutputStream()) {
            TaskContext context = new TaskContext(dispatcher.get(), storageProvider, export.getId());
            SiteExporter exporter = new SiteExporter(context).buildExcelWorkbook(filter);
            exporter.getBook().write(out);
        } catch (ColumnSizeException e) {
            ApiError error = new ApiError(ApiErrorType.VALIDATION_ERROR, ApiErrorCode.EXPORT_COLUMN_LIMIT_REACHED);
            error.setMessage(I18N.MESSAGES.activityColumnLimitExceeded(e.getFormLabel(), e.getColLimit()));
            throw new ApiException(error.toJson().toJson());
        }

        return new ExportResult(export.getDownloadUri());
    }

    private String fileName() {
        String date = new SimpleDateFormat("YYYY-MM-dd_HHmmss").format(new Date());
        return ("ActivityInfo_Export_" + date + ".xls").replace(" ", "_");
    }

}