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
import org.activityinfo.store.spi.FormCatalog;

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
    private final FormCatalog catalog;

    @Inject
    public ExportAuditLogExecutor(DispatcherSync dispatcher, Provider<EntityManager> entityManager, StorageProvider storageProvider, FormCatalog catalog) {
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
                logWriter.writeForm(catalog, activityDTO.getFormClassId());
            }
        }

        return new ExportResult(export.getDownloadUri());
    }
}
