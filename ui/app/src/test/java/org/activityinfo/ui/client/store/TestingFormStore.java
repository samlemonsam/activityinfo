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
import org.activityinfo.model.analysis.Analysis;
import org.activityinfo.model.analysis.AnalysisUpdate;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableTree;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.testing.TestingStorageProvider;
import org.activityinfo.ui.client.store.offline.FormOfflineStatus;

import java.util.*;
import java.util.function.Supplier;

/**
 * A FormStore implementation that can be used for unit tests.
 */
public class TestingFormStore implements FormStore {


    private static class PendingTask<T> {
        private Supplier<T> task;
        private StatefulValue<T> result;

        public PendingTask(Supplier<T> task) {
            this.task = task;
            this.result = new StatefulValue<T>();
        }

        public Observable<T> getResult() {
            return result;
        }

        public void execute() {
            result.updateValue(task.get());
        }

    }

    private TestingStorageProvider testingCatalog;
    private Set<ResourceId> deleted = new HashSet<>();

    private boolean delayLoading = false;
    private ArrayDeque<PendingTask<?>> pendingTasks = new ArrayDeque<>();

    public TestingFormStore() {
        testingCatalog = new TestingStorageProvider();
    }

    public TestingStorageProvider getCatalog() {
        return testingCatalog;
    }


    public void delayLoading() {
        delayLoading = true;
    }

    public void loadAll() {

        while(!pendingTasks.isEmpty()) {
            pendingTasks.pop().execute();
        }
    }

    @Override
    public Observable<FormMetadata> getFormMetadata(ResourceId formId) {
        return maybeExecute(() -> fetchFormMetadata(formId));
    }

    @Override
    public Observable<Maybe<RecordTree>> getRecordTree(RecordRef rootRecordId) {
        throw new UnsupportedOperationException("TODO");
    }

    private FormMetadata fetchFormMetadata(ResourceId formId) {
        if(deleted.contains(formId)) {
            return FormMetadata.forbidden(formId);

        } else {
            return FormMetadata.of(1L, testingCatalog.getFormClass(formId), FormPermissions.readWrite());
        }
    }

    @Override
    public Promise<Void> deleteForm(ResourceId formId) {
        deleted.add(formId);
        return Promise.resolved(null);
    }

    @Override
    public Observable<List<CatalogEntry>> getCatalogRoots() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<List<CatalogEntry>> getCatalogChildren(ResourceId parentId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<List<FormRecord>> getSubRecords(ResourceId formId, RecordRef parent) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<FormTree> getFormTree(ResourceId formId) {
        return new ObservableTree<>(new FormTreeLoader(formId, id -> getFormMetadata(id)), new ImmediateScheduler());
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        return maybeExecute(() -> testingCatalog.query(queryModel));
    }

    @Override
    public Observable<Maybe<Analysis>> getAnalysis(String id) {
        return Observable.just(Maybe.notFound());
    }

    @Override
    public Observable<Maybe<FormRecord>> getRecord(RecordRef recordRef) {
        return maybeExecute(() -> {
            Optional<FormStorage> storage = testingCatalog.getForm(recordRef.getFormId());
            if(!storage.isPresent()) {
                return Maybe.notFound();
            }
            Optional<FormRecord> record = storage.get().get(recordRef.getRecordId());
            if(!record.isPresent()) {
                return Maybe.notFound();
            }
            return Maybe.of(record.get());
        });
    }

    @Override
    public void setFormOffline(ResourceId formId, boolean offline) {

    }

    @Override
    public Observable<FormOfflineStatus> getOfflineStatus(ResourceId formId) {
        return Observable.just(new FormOfflineStatus(false, false));
    }

    @Override
    public Observable<RecordHistory> getFormRecordHistory(RecordRef ref) {
        return Observable.just(RecordHistory.create(Collections.emptyList()));
    }

    @Override
    public Promise<Void> updateRecords(RecordTransaction tx) {
        testingCatalog.updateRecords(tx);
        return Promise.done();
    }

    @Override
    public Promise<Void> updateAnalysis(AnalysisUpdate update) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public <T extends JobDescriptor<R>, R extends JobResult> Observable<JobStatus<T, R>> startJob(T job) {
        return Observable.loading();
    }

    private <T> Observable<T> maybeExecute(Supplier<T> task) {
        if (delayLoading) {
            PendingTask<T> pending = new PendingTask<T>(task);
            pendingTasks.add(pending);
            return pending.getResult();
        } else {
            return Observable.just(task.get());
        }
    }
}