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

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.job.ExportAuditLog;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.endpoint.rest.CsvWriter;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.store.spi.FormStorageProvider;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Exports an audit log for a given database.
 */
public class ExportAuditLogExecutor implements JobExecutor<ExportAuditLog, ExportResult> {

    private final DispatcherSync dispatcher;
    private final Provider<EntityManager> entityManager;
    private final StorageProvider storageProvider;
    private final FormStorageProvider catalog;

    @Inject
    public ExportAuditLogExecutor(DispatcherSync dispatcher, Provider<EntityManager> entityManager, StorageProvider storageProvider, FormStorageProvider catalog) {
        this.dispatcher = dispatcher;
        this.entityManager = entityManager;
        this.storageProvider = storageProvider;
        this.catalog = catalog;
    }

    @Override
    public ExportResult execute(ExportAuditLog descriptor) throws IOException {
        UserDatabaseDTO db = dispatcher.execute(new GetSchema()).getDatabaseById(descriptor.getDatabaseId());

        GeneratedResource export = storageProvider.create("text/csv;charset=UTF-8",
                String.format("AuditLog_%d_%s.csv", db.getId(), Filenames.timestamp()));

        try(CsvWriter  writer = new CsvWriter(new OutputStreamWriter(export.openOutputStream(), Charsets.UTF_8))) {
            AuditLogWriter logWriter = new AuditLogWriter(entityManager.get(), db, writer);
            for (ActivityDTO activityDTO : db.getActivities()) {
                logWriter.writeForm(catalog, activityDTO.getFormId());
            }
        }

        return new ExportResult(export.getDownloadUri());
    }
}
