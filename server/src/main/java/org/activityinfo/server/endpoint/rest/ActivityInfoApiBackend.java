package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.store.hrd.HrdSerialNumberProvider;
import org.activityinfo.store.mysql.MySqlCatalog;
import org.activityinfo.store.mysql.MySqlRecordHistoryBuilder;
import org.activityinfo.store.query.server.*;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.server.ApiBackend;
import org.activityinfo.store.spi.BlobAuthorizer;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.RecordHistoryProvider;

public class ActivityInfoApiBackend implements ApiBackend {

    private final Injector injector;

    @Inject
    public ActivityInfoApiBackend(Injector injector) {
        this.injector = injector;
    }

    @Override
    public FormCatalog getCatalog() {
        return injector.getInstance(FormCatalog.class);
    }

    @Override
    public FormSupervisor getFormSupervisor() {
        return new FormSupervisorAdapter(getCatalog(), getAuthenticatedUserId());
    }

    @Override
    public int getAuthenticatedUserId() {
        return getAuthenticatedUser().getUserId();
    }

    private AuthenticatedUser getAuthenticatedUser() {
        return injector.getInstance(AuthenticatedUser.class);
    }

    @Override
    public void createNewForm(FormClass formClass) {
        // Check that we have the permission to create in this database
        PermissionOracle permissionOracle = injector.getInstance(PermissionOracle.class);
        permissionOracle.assertDesignPrivileges(formClass, getAuthenticatedUser());

        ((MySqlCatalog)getCatalog()).createOrUpdateFormSchema(formClass);
    }

    @Override
    public Updater newUpdater() {
        return new Updater(getCatalog(), getAuthenticatedUser().getUserId(),
                injector.getInstance(BlobAuthorizer.class),
                new HrdSerialNumberProvider());
    }

    @Override
    public ColumnSetBuilder newQueryBuilder() {
        return new ColumnSetBuilder(getCatalog(), new AppEngineFormScanCache(), getFormSupervisor());
    }

    @Override
    public PermissionsEnforcer newPermissionsEnforcer() {
        return new PermissionsEnforcer(injector.getInstance(FormCatalog.class), getAuthenticatedUser().getUserId());
    }

    @Override
    public RecordHistoryProvider getRecordHistoryProvider() {
        return new MySqlRecordHistoryBuilder((MySqlCatalog) getCatalog());
    }
}
