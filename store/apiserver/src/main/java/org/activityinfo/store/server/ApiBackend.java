package org.activityinfo.store.server;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.PermissionsEnforcer;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.RecordHistoryProvider;

/**
 * Temporary interface to split out dependencies on ActivityInfo
 */
public interface ApiBackend {

    FormStorageProvider getStorage();

    FormCatalog getCatalog();

    FormSupervisor getFormSupervisor();

    int getAuthenticatedUserId();

    void createNewForm(FormClass formClass);

    Updater newUpdater();

    ColumnSetBuilder newQueryBuilder();

    PermissionsEnforcer newPermissionsEnforcer();

    RecordHistoryProvider getRecordHistoryProvider();

}
