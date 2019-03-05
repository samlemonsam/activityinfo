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
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.TransactionMode;
import org.activityinfo.store.hrd.AppEngineFormScanCache;
import org.activityinfo.store.hrd.HrdSerialNumberProvider;
import org.activityinfo.store.mysql.MySqlRecordHistoryBuilder;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.query.UsageTracker;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.FormSupervisorAdapter;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.*;

import java.util.Optional;
import java.util.logging.Logger;

public class ActivityInfoApiBackend implements ApiBackend {

    private static final Logger LOGGER = Logger.getLogger(ActivityInfoApiBackend.class.getName());

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
        return new FormSupervisorAdapter(getStorage(), getDatabaseProvider(), getAuthenticatedUserId());
    }

    @Override
    public DatabaseProvider getDatabaseProvider() {
        return injector.getInstance(DatabaseProvider.class);
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
        DatabaseProvider databaseProvider = injector.getInstance(DatabaseProvider.class);
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        Optional<UserDatabaseMeta> databaseMeta = databaseProvider.getDatabaseMetadata(
                formClass.getDatabaseId(),
                authenticatedUser.getUserId());

        assertCreateFormRights(formClass, databaseMeta);

        ((MySqlStorageProvider) getStorage()).createOrUpdateFormSchema(formClass);

        UsageTracker.track(getAuthenticatedUserId(), "create_form", formClass);
    }

    private void assertCreateFormRights(FormClass formClass, Optional<UserDatabaseMeta> dbMeta) {
        if (!dbMeta.isPresent()) {
            throw new IllegalArgumentException("Database must exist");
        }
        UserDatabaseMeta databaseMeta = dbMeta.get();
        ResourceId containerResource = formClass.getParentFormId().or(formClass.getDatabaseId());
        if (!PermissionOracle.canCreateForm(containerResource, databaseMeta)) {
            LOGGER.severe(() -> String.format("User %d does not have "
                            + Operation.CREATE_RESOURCE.name()
                            + " rights in container resource %s"
                            + " on Database %s",
                    databaseMeta.getUserId(),
                    containerResource,
                    databaseMeta.getDatabaseId()));
            throw new IllegalAccessCommandException();
        }
    }

    @Override
    public Updater newUpdater(TransactionMode mode) {
        return new Updater(getStorage(),
                getDatabaseProvider(),
                injector.getInstance(BlobAuthorizer.class),
                new HrdSerialNumberProvider(),
                getAuthenticatedUser().getUserId(),
                mode);
    }

    @Override
    public ColumnSetBuilder newQueryBuilder() {
        return new ColumnSetBuilder(getStorage(), new AppEngineFormScanCache(), getFormSupervisor());
    }

    @Override
    public BatchingFormTreeBuilder newBatchingTreeBuilder() {

        return new BatchingFormTreeBuilder(getStorage(), getFormSupervisor(),
                com.google.common.base.Optional.of(injector.getInstance(BillingAccountOracle.class)),
                getAuthenticatedUserId());
    }

    @Override
    public RecordHistoryProvider getRecordHistoryProvider() {
        return new MySqlRecordHistoryBuilder((MySqlStorageProvider) getStorage());
    }
}
