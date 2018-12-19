package org.activityinfo.server.job;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.io.xls.XlsTableWriter;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.FilterUrlSerializer;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.error.ApiError;
import org.activityinfo.model.error.ApiErrorCode;
import org.activityinfo.model.error.ApiErrorType;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.job.ExportSitesJob;
import org.activityinfo.model.error.ApiException;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.endpoint.export.ColumnSizeException;
import org.activityinfo.server.endpoint.export.SiteExporter;
import org.activityinfo.server.endpoint.export.TaskContext;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.store.spi.DatabaseProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ExportSitesExecutor implements JobExecutor<ExportSitesJob, ExportResult> {

    private static final Logger LOGGER = Logger.getLogger(ExportSitesExecutor.class.getName());

    private StorageProvider storageProvider;
    private DatabaseProvider databaseProvider;
    private Provider<DispatcherSync> dispatcher;
    private Provider<AuthenticatedUser> authUser;

    @Inject
    public ExportSitesExecutor(StorageProvider storageProvider,
                               DatabaseProvider databaseProvider,
                               Provider<DispatcherSync> dispatcher,
                               Provider<AuthenticatedUser> authUser) {
        this.storageProvider = storageProvider;
        this.databaseProvider = databaseProvider;
        this.dispatcher = dispatcher;
        this.authUser = authUser;
    }

    @Override
    public ExportResult execute(ExportSitesJob descriptor) throws IOException {
        Filter filter = FilterUrlSerializer.fromUrlFragment(descriptor.getFilter());

        authorizeExport(filter);

        // Create a unique key from which the user can retrieve the file from GCS
        GeneratedResource export = storageProvider.create(XlsTableWriter.EXCEL_MIME_TYPE, fileName());
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

    private void authorizeExport(Filter filter) {
        List<ResourceId> databaseIds = new ArrayList<>();
        List<ResourceId> activityIds = new ArrayList<>();
        if (filter.isRestricted(DimensionType.Database)) {
            databaseIds.addAll(filter.getRestrictions(DimensionType.Database).stream()
                    .map(CuidAdapter::databaseId)
                    .collect(Collectors.toSet()));
        }
        if (filter.isRestricted(DimensionType.Activity)) {
            activityIds.addAll(filter.getRestrictions(DimensionType.Activity).stream()
                    .map(CuidAdapter::activityFormClass)
                    .collect(Collectors.toSet()));
        }
        databaseIds.forEach(this::checkExportRightsOnAllResources);
        activityIds.forEach(this::checkExportRightsOnActivity);
    }

    private void checkExportRightsOnAllResources(ResourceId databaseId) {
        Optional<UserDatabaseMeta> dbMeta = databaseProvider.getDatabaseMetadata(databaseId, authUser.get().getUserId());

        if (!dbMeta.isPresent()) {
            ApiError error = new ApiError(ApiErrorType.INVALID_REQUEST_ERROR, ApiErrorCode.DATABASE_NOT_FOUND);
            throw new ApiException(error.toJson().toJson());
        }
        for (Resource resource : dbMeta.get().getResources()) {
            if (!PermissionOracle.canExportRecords(resource.getId(), dbMeta.get())) {
                ApiError error = new ApiError(ApiErrorType.AUTHORIZATION_ERROR, ApiErrorCode.EXPORT_ACTIVITIES_FORBIDDEN);
                throw new ApiException(error.toJson().toJson());
            }
        }
    }

    private void checkExportRightsOnActivity(ResourceId activityId) {
        Optional<UserDatabaseMeta> dbMeta = databaseProvider.getDatabaseMetadataByResource(activityId, authUser.get().getUserId());

        if (!dbMeta.isPresent()) {
            ApiError error = new ApiError(ApiErrorType.INVALID_REQUEST_ERROR, ApiErrorCode.DATABASE_NOT_FOUND);
            throw new ApiException(error.toJson().toJson());
        }
        if (!PermissionOracle.canExportRecords(activityId, dbMeta.get())) {
            ApiError error = new ApiError(ApiErrorType.AUTHORIZATION_ERROR, ApiErrorCode.EXPORT_ACTIVITIES_FORBIDDEN);
            throw new ApiException(error.toJson().toJson());
        }
    }

    private String fileName() {
        String date = new SimpleDateFormat("YYYY-MM-dd_HHmmss").format(new Date());
        return ("ActivityInfo_Export_" + date + ".xls").replace(" ", "_");
    }

}
