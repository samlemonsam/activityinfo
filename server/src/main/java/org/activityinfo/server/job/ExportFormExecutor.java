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
package org.activityinfo.server.job;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.ExportViewModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.io.csv.CsvTableWriter;
import org.activityinfo.io.xls.XlsTableWriter;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.analysis.table.ExportFormat;
import org.activityinfo.model.analysis.table.UnsupportedExportFormatException;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.error.ApiError;
import org.activityinfo.model.error.ApiErrorCode;
import org.activityinfo.model.error.ApiErrorType;
import org.activityinfo.model.error.ApiException;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.spi.DatabaseProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class ExportFormExecutor implements JobExecutor<ExportFormJob, ExportResult> {

    private final FormSource formSource;
    private final StorageProvider storageProvider;
    private final DatabaseProvider databaseProvider;
    private final AuthenticatedUser authUser;

    @Inject
    public ExportFormExecutor(FormSource formSource,
                              StorageProvider storageProvider,
                              DatabaseProvider databaseProvider,
                              AuthenticatedUser authUser) {
        this.formSource = formSource;
        this.storageProvider = storageProvider;
        this.databaseProvider = databaseProvider;
        this.authUser = authUser;
    }

    @Override
    public ExportResult execute(ExportFormJob descriptor) throws IOException {

        authorizeExport(descriptor);

        TableModel tableModel = descriptor.getTableModel();
        ExportFormat format = descriptor.getFormat();

        TableViewModel viewModel = new TableViewModel(formSource, tableModel);
        EffectiveTableModel effectiveTableModel = viewModel.getEffectiveTable().waitFor();

        if (ExportViewModel.columnLimitExceeded(effectiveTableModel, descriptor.getFormat())) {
            throw new IOException("Current column length " + ExportViewModel.exportedColumnSize(effectiveTableModel)
                    + " exceeds " + format.name() + " Column Limitation of " + format.getColumnLimit());
        }

        switch (format) {
            case CSV:
                return csvExport(effectiveTableModel);
            case XLS:
                return xlsExport(effectiveTableModel);
            default:
                throw new UnsupportedExportFormatException(format.name());
        }
    }

    private void authorizeExport(ExportFormJob descriptor) {
        Optional<UserDatabaseMeta> db = databaseProvider.getDatabaseMetadataByResource(descriptor.getFormId(), authUser.getUserId());
        if (!db.isPresent()) {
            ApiError error = new ApiError(ApiErrorType.INVALID_REQUEST_ERROR, ApiErrorCode.DATABASE_NOT_FOUND);
            throw new ApiException(error.toJson().toJson());
        }
        if (!PermissionOracle.canExportRecords(descriptor.getFormId(), db.get())) {
            ApiError error = new ApiError(ApiErrorType.AUTHORIZATION_ERROR, ApiErrorCode.EXPORT_FORMS_FORBIDDEN);
            throw new ApiException(error.toJson().toJson());
        }
    }

    private ExportResult xlsExport(EffectiveTableModel effectiveTableModel) throws IOException {
        ColumnSet columnSet = effectiveTableModel.getColumnSet().waitFor();
        String fileName = fileName(effectiveTableModel.getFormLabel(), ".xls");
        GeneratedResource export = storageProvider.create(XlsTableWriter.EXCEL_MIME_TYPE, fileName);

        XlsTableWriter writer = new XlsTableWriter();
        writer.addSheet(effectiveTableModel, columnSet);

        try(OutputStream out = export.openOutputStream()) {
            writer.write(out);
        }

        return new ExportResult(export.getDownloadUri());
    }

    private ExportResult csvExport(EffectiveTableModel effectiveTableModel) throws IOException {
        ColumnSet columnSet = effectiveTableModel.getColumnSet().waitFor();
        String fileName = fileName(effectiveTableModel.getFormLabel(), ".csv");
        GeneratedResource export = storageProvider.create(CsvTableWriter.CSV_MIME_TYPE, fileName);

        try(CsvTableWriter writer = new CsvTableWriter(new OutputStreamWriter(export.openOutputStream(), "UTF-8"))) {
            writer.writeTable(effectiveTableModel, columnSet);
        }

        return new ExportResult(export.getDownloadUri());
    }

    private String fileName(String formName, String fileExtension) {
        String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
        String unescaped = "ActivityInfo_Export_" + formName + "_" + date + fileExtension;
        return UrlEscapers.urlPathSegmentEscaper().escape(unescaped);
    }

}
