package chdc.server;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.PermissionsEnforcer;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.server.ApiBackend;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.RecordHistoryProvider;

public class ChdcApiBackend implements ApiBackend {
    @Override
    public FormCatalog getCatalog() {
        return new ChdcCatalog();
    }

    @Override
    public FormSupervisor getFormSupervisor() {
        return new NullFormSupervisor();
    }

    @Override
    public int getAuthenticatedUserId() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void createNewForm(FormClass formClass) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Updater newUpdater() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ColumnSetBuilder newQueryBuilder() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PermissionsEnforcer newPermissionsEnforcer() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RecordHistoryProvider getRecordHistoryProvider() {
        throw new UnsupportedOperationException("TODO");
    }
}
