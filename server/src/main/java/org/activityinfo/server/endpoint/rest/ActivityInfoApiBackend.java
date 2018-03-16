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
package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.store.hrd.AppEngineFormScanCache;
import org.activityinfo.store.hrd.HrdSerialNumberProvider;
import org.activityinfo.store.mysql.MySqlRecordHistoryBuilder;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.FormSupervisorAdapter;
import org.activityinfo.store.query.server.PermissionsEnforcer;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.BlobAuthorizer;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.RecordHistoryProvider;

public class ActivityInfoApiBackend implements ApiBackend {

    private final Injector injector;

    @Inject
    public ActivityInfoApiBackend(Injector injector) {
        this.injector = injector;
    }

    @Override
    public FormStorageProvider getStorage() {
        return injector.getInstance(FormStorageProvider.class);
    }

    @Override
    public FormCatalog getCatalog() {
        return injector.getInstance(FormCatalog.class);
    }

    @Override
    public FormSupervisor getFormSupervisor() {
        return new FormSupervisorAdapter(getStorage(), getAuthenticatedUserId());
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

        ((MySqlStorageProvider) getStorage()).createOrUpdateFormSchema(formClass);
    }

    @Override
    public Updater newUpdater() {
        return new Updater(getStorage(), getAuthenticatedUser().getUserId(),
                injector.getInstance(BlobAuthorizer.class),
                new HrdSerialNumberProvider());
    }

    @Override
    public ColumnSetBuilder newQueryBuilder() {
        return new ColumnSetBuilder(getStorage(), new AppEngineFormScanCache(), getFormSupervisor());
    }

    @Override
    public PermissionsEnforcer newPermissionsEnforcer() {
        return new PermissionsEnforcer(injector.getInstance(FormStorageProvider.class), getAuthenticatedUser().getUserId());
    }

    @Override
    public RecordHistoryProvider getRecordHistoryProvider() {
        return new MySqlRecordHistoryBuilder((MySqlStorageProvider) getStorage());
    }
}
