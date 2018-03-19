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
package org.activityinfo.ui.client.store;

import com.google.common.base.Optional;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.FormRecordUpdateBuilder;
import org.activityinfo.api.client.NewFormRecordBuilder;
import org.activityinfo.model.analysis.Analysis;
import org.activityinfo.model.analysis.AnalysisUpdate;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.VersionedFormStorage;
import org.activityinfo.store.testing.TestingStorageProvider;

import java.util.List;

public class AsyncClientStub implements ActivityInfoClientAsync {

    private TestingStorageProvider catalog;
    private boolean connected = true;

    public AsyncClientStub() {
        this.catalog = new TestingStorageProvider();
    }

    public AsyncClientStub(TestingStorageProvider testingCatalog) {
        this.catalog = testingCatalog;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public Promise<List<CatalogEntry>> getFormCatalog(String parent) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    public TestingStorageProvider getCatalog() {
        return catalog;
    }

    @Override
    public Promise<Maybe<FormRecord>> getRecord(String formId, String recordId) {

        if(!connected) {
            return offlineResult();
        }
        Optional<FormStorage> form = catalog.getForm(ResourceId.valueOf(formId));
        if(!form.isPresent()) {
            return Promise.resolved(Maybe.notFound());
        }

        return Promise.resolved(Maybe.fromOptional(form.get().get(ResourceId.valueOf(recordId))));
    }

    @Override
    public Promise<Void> updateRecord(String formId, String recordId, FormRecordUpdateBuilder update) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<RecordHistory> getRecordHistory(String formId, String recordId) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<FormRecordSet> getRecords(String formId, String parentId) {
        if(!connected) {
            return offlineResult();
        }
        Optional<FormStorage> form = catalog.getForm(ResourceId.valueOf(formId));
        if(!form.isPresent()) {
            return Promise.rejected(new RuntimeException("No such form"));
        }
        FormStorage formStorage = form.get();
        return Promise.resolved(new FormRecordSet(form.get().getFormClass().getId(), formStorage.getSubRecords(ResourceId.valueOf(parentId))));
    }

    @Override
    public Promise<FormSyncSet> getRecordVersionRange(String formId, long localVersion, long toVersion) {
        if(!connected) {
            return offlineResult();
        }
        Optional<FormStorage> form = catalog.getForm(ResourceId.valueOf(formId));
        if(!form.isPresent()) {
            return Promise.rejected(new RuntimeException("No such form"));
        }
        VersionedFormStorage formStorage = (VersionedFormStorage) form.get();
        return Promise.resolved(formStorage.getVersionRange(localVersion, toVersion, resourceId -> true));
    }

    @Override
    public Promise<Void> createRecord(String formId, NewFormRecordBuilder newRecord) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<FormClass> getFormSchema(String formId) {
        if(!connected) {
            return offlineResult();
        }

        Optional<FormStorage> formSchema = catalog.getForm(ResourceId.valueOf(formId));
        if(formSchema.isPresent()) {
            return Promise.resolved(formSchema.get().getFormClass());
        } else {
            return Promise.rejected(new UnsupportedOperationException("No such form: " + formId));
        }
    }

    @Override
    public Promise<FormMetadata> getFormMetadata(String formId) {
        if(!connected) {
            return offlineResult();
        }

        Optional<FormStorage> form = catalog.getForm(ResourceId.valueOf(formId));
        if(!form.isPresent()) {
            return Promise.resolved(FormMetadata.notFound(ResourceId.valueOf(formId)));
        } else {
            return Promise.resolved(FormMetadata.of(
                form.get().cacheVersion(),
                form.get().getFormClass(),
                FormPermissions.readWrite()));
        }
    }

    @Override
    public Promise<FormTree> getFormTree(ResourceId formId) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<Void> updateFormSchema(String formId, FormClass updatedSchema) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<ColumnSet> queryTableColumns(QueryModel query) {

        if(!connected) {
            return offlineResult();
        }

        ColumnSetBuilder columnSetBuilder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());
        ColumnSet columnSet = columnSetBuilder.build(query);

        return Promise.resolved(columnSet);
    }

    @Override
    public Promise<Void> updateRecords(RecordTransaction transactions) {
        if(!connected) {
            return offlineResult();
        }

        catalog.updateRecords(transactions);

        return Promise.done();
    }

    @Override
    public Promise<Maybe<Analysis>> getAnalysis(String id) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<Void> updateAnalysis(AnalysisUpdate analysis) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public <T extends JobDescriptor<R>, R extends JobResult> Promise<JobStatus<T, R>> startJob(T job) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<JobStatus<?, ?>> getJobStatus(String jobId) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    private <T> Promise<T> offlineResult() {
        return Promise.rejected(new RuntimeException("Offline"));
    }

}
