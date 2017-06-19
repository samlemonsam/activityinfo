package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.TransactionBuilder;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.testing.TestingCatalog;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private TestingCatalog testingCatalog;
    private Set<ResourceId> deleted = new HashSet<>();

    private boolean delayLoading = false;
    private ArrayDeque<PendingTask<?>> pendingTasks = new ArrayDeque<>();

    public TestingFormStore() {
        testingCatalog = new TestingCatalog();
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

    private FormMetadata fetchFormMetadata(ResourceId formId) {
        FormMetadata metadata = new FormMetadata();
        metadata.setId(formId);
        metadata.setVersion(1);

        if(deleted.contains(formId)) {
            metadata.setDeleted(true);
            metadata.setVersion(2);

        } else {
            metadata.setSchema(testingCatalog.getFormClass(formId));
        }
        return metadata;
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
    public Observable<FormTree> getFormTree(ResourceId formId) {
        return new ObservableFormTree(formId, id -> getFormMetadata(id), new ImmediateScheduler());
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        return maybeExecute(() -> testingCatalog.query(queryModel));
    }

    @Override
    public Observable<FormRecord> getRecord(RecordRef recordRef) {
        return maybeExecute(() -> testingCatalog.getForm(recordRef.getFormId()).get().get(recordRef.getRecordId()).get());
    }

    @Override
    public void setFormOffline(ResourceId formId, boolean offline) {

    }

    @Override
    public Observable<OfflineStatus> getOfflineStatus(ResourceId formId) {
        return Observable.just(new OfflineStatus(false, false));
    }

    @Override
    public Promise<Void> updateRecords(TransactionBuilder transactionBuilder) {
        testingCatalog.updateRecords(transactionBuilder);
        return Promise.done();
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