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
package org.activityinfo.store.query.server;

import com.google.common.base.Optional;
import org.activityinfo.model.analysis.Analysis;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormMetadataProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.query.shared.FormScanCache;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.spi.DatabaseProvider;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.store.spi.FormStorage;

/**
 * Synchronous implementation of the {@link FormSource} interface, that
 * also ensures permissions are respected.
 */
public class FormSourceSyncImpl implements FormSource {

    private FormStorageProvider formCatalog;
    private FormScanCache formScanCache;
    private DatabaseProvider databaseProvider;
    private int userId;

    public FormSourceSyncImpl(FormStorageProvider formCatalog, FormScanCache cache, DatabaseProvider databaseProvider, int userId) {
        this.formCatalog = formCatalog;
        this.formScanCache = cache;
        this.databaseProvider = databaseProvider;
        this.userId = userId;
    }

    public FormSourceSyncImpl(FormStorageProvider formCatalog, DatabaseProvider databaseProvider, int userId) {
        this(formCatalog, new NullFormScanCache(), databaseProvider, userId);
    }

    public FormMetadata getFormMetadata(ResourceId formId) {
        Optional<FormStorage> storage = formCatalog.getForm(formId);
        if(!storage.isPresent()) {
            return FormMetadata.notFound(formId);
        }

        ResourceId databaseId = storage.get().getFormClass().getDatabaseId();
        java.util.Optional<UserDatabaseMeta> databaseMeta = databaseProvider.getDatabaseMetadata(databaseId, userId);
        if (!databaseMeta.isPresent()) {
            return FormMetadata.notFound(formId);
        }
        FormPermissions permissions = PermissionOracle.formPermissions(formId, databaseMeta.get());

        if(!permissions.isVisible()) {
            return FormMetadata.forbidden(formId);
        }

        return FormMetadata.of(
            storage.get().cacheVersion(),
            storage.get().getFormClass(),
            permissions);
    }


    @Override
    public Observable<FormTree> getFormTree(ResourceId formId) {
        FormTreeBuilder builder = new FormTreeBuilder(new FormMetadataProvider() {
            @Override
            public FormMetadata getFormMetadata(ResourceId formId) {
                return FormSourceSyncImpl.this.getFormMetadata(formId);
            }
        });
        return Observable.just(builder.queryTree(formId));
    }

    @Override
    public Observable<Maybe<RecordTree>> getRecordTree(RecordRef recordRef) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<Maybe<FormRecord>> getRecord(RecordRef recordRef) {
        return Observable.loading();
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(formCatalog, formScanCache,
                new FormSupervisorAdapter(formCatalog, databaseProvider, userId));
        return Observable.just(builder.build(queryModel));
    }

    @Override
    public Observable<Maybe<Analysis>> getAnalysis(String id) {
        throw new UnsupportedOperationException();
    }
}
