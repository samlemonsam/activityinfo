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
import org.activityinfo.store.query.shared.FormSource;

import java.io.IOException;
import java.io.OutputStream;

public class ExportFormExecutor implements JobExecutor<ExportFormJob, ExportResult> {

    private final FormSource formSource;
    private final StorageProvider storageProvider;


    @Inject
    public ExportFormExecutor(FormSource formSource, StorageProvider storageProvider) {
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
