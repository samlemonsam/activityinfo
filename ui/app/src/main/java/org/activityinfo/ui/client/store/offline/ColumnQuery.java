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
package org.activityinfo.ui.client.store.offline;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.indexedb.IDBTransaction;
import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.indexedb.VoidWork;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.client.columns.JsColumnFactory;
import org.activityinfo.store.query.shared.*;
import org.activityinfo.store.spi.NullFormVersionProvider;
import org.activityinfo.store.spi.Slot;
import org.activityinfo.ui.client.store.tasks.Task;
import org.activityinfo.ui.client.store.tasks.TaskExecution;

/**
 * Executes a QueryModel query against IndexedDB
 */
public class ColumnQuery implements Task<ColumnSet> {

    private final OfflineDatabase executor;
    private final FormTree formTree;
    private final QueryModel queryModel;

    public ColumnQuery(OfflineDatabase executor, FormTree formTree, QueryModel queryModel) {
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
            FormScanBatch batch = new FormScanBatch(
                    JsColumnFactory.INSTANCE,
                    formTree, NullFormVersionProvider.INSTANCE,
                    new NullFormSupervisor());

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
