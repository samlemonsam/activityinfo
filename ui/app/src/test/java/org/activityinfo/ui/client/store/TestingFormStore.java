package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.store.testing.TestingCatalog;

import java.util.ArrayDeque;
import java.util.List;
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
    public Observable<FormClass> getFormClass(ResourceId formId) {
        return maybeExecute(() -> testingCatalog.getFormClass(formId));
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
        return maybeExecute(() -> {
            FormTreeBuilder builder = new FormTreeBuilder(testingCatalog);
            return builder.queryTree(formId);
        });
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        return maybeExecute(() -> testingCatalog.query(queryModel));
    }

    @Override
    public Observable<FormRecord> getRecord(RecordRef recordRef) {
        return maybeExecute(() -> testingCatalog.getForm(recordRef.getFormId()).get().get(recordRef.getRecordId()).get());
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