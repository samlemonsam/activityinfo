package org.activityinfo.ui.client.store.offline;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.shared.*;
import org.activityinfo.ui.client.store.tasks.Task;
import org.activityinfo.ui.client.store.tasks.TaskExecution;

/**
 * Executes a QueryModel query against IndexedDB
 */
public class ColumnQuery implements Task<ColumnSet> {

    private final IDBExecutor executor;
    private final FormTree formTree;
    private final QueryModel queryModel;

    public ColumnQuery(IDBExecutor executor, FormTree formTree, QueryModel queryModel) {
        this.executor = executor;
        this.formTree = formTree;
        this.queryModel = queryModel;
    }

    @Override
    public TaskExecution start(AsyncCallback<ColumnSet> callback) {
        return new Execution(callback);
    }

    @Override
    public int refreshInterval(ColumnSet result) {
        return 0;
    }

    private class Execution implements TaskExecution {

        private boolean running = true;
        private boolean cancelled = false;
        private AsyncCallback<ColumnSet> callback;

        public Execution(AsyncCallback<ColumnSet> callback) {
            this.callback = callback;

            // Queue up the data we need
            FormScanBatch batch = new FormScanBatch(formTree, new NullFormSupervisor());
            QueryEvaluator evaluator = new QueryEvaluator(FilterLevel.PERMISSIONS, formTree, batch);
            Slot<ColumnSet> queryResult = evaluator.evaluate(queryModel);

            // Now execute the actual queries

            executor.begin(RecordStore.NAME).execute(new VoidWork() {
                @Override
                public void execute(IDBTransaction tx) {
                    for (FormScan formScan : batch.getScans()) {
                        FormClass formClass = formTree.getFormClass(formScan.getFormId());
                        QueryRunner runner = new QueryRunner(formClass, tx);
                        formScan.prepare(runner);
                        runner.execute();
                    }
                }
            }).then(new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    if(!cancelled) {
                        callback.onFailure(caught);
                    }
                }

                @Override
                public void onSuccess(Void result) {
                    if(!cancelled) {
                        callback.onSuccess(queryResult.get());
                    }
                }
            });
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }


}
